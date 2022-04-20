package uk.gov.justice.digital.hmpps.manageoffencesapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.manageoffencesapi.entity.SdrsLoadResultHistory
import uk.gov.justice.digital.hmpps.manageoffencesapi.enum.LoadStatus
import uk.gov.justice.digital.hmpps.manageoffencesapi.enum.LoadStatus.FAIL
import uk.gov.justice.digital.hmpps.manageoffencesapi.enum.LoadStatus.SUCCESS
import uk.gov.justice.digital.hmpps.manageoffencesapi.enum.LoadType
import uk.gov.justice.digital.hmpps.manageoffencesapi.enum.LoadType.FULL_LOAD
import uk.gov.justice.digital.hmpps.manageoffencesapi.enum.LoadType.UPDATE
import uk.gov.justice.digital.hmpps.manageoffencesapi.enum.SdrsErrorCodes.SDRS_99918
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.GatewayOperationTypeRequest
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.GetControlTableRequest
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.GetOffenceRequest
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.MessageBodyRequest
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.MessageHeader
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.MessageID
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.Offence
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.SDRSRequest
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs.SDRSResponse
import uk.gov.justice.digital.hmpps.manageoffencesapi.repository.OffenceRepository
import uk.gov.justice.digital.hmpps.manageoffencesapi.repository.SdrsLoadStatusHistoryRepository
import uk.gov.justice.digital.hmpps.manageoffencesapi.repository.SdrsLoadStatusRepository
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.EntityNotFoundException

@Service
class SDRSService(
  private val sdrsApiClient: SDRSApiClient,
  private val offenceRepository: OffenceRepository,
  private val sdrsLoadStatusRepository: SdrsLoadStatusRepository,
  private val sdrsLoadStatusHistoryRepository: SdrsLoadStatusHistoryRepository,
) {
  private fun findAllOffencesByAlphaChar(alphaChar: Char): SDRSResponse {
    // TODO at the moment this fetches ALL offences, which means multiple revisions of the same offence could be returned
    //  we may need to sanitise so that only the latest version of each offence is stored
    val sdrsRequest = createOffenceRequest(alphaChar = alphaChar)
    return sdrsApiClient.callSDRS(sdrsRequest)
  }

  private fun findUpdatedOffences(alphaChar: Char, lastUpdatedDate: LocalDateTime): SDRSResponse {
    val sdrsRequest = createOffenceRequest(alphaChar = alphaChar, changedDate = lastUpdatedDate)
    return sdrsApiClient.callSDRS(sdrsRequest)
  }

  @Transactional
  fun loadAllOffences() {
    offenceRepository.deleteAll()
    val loadDate = LocalDateTime.now()
    ('A'..'Z').forEach { alphaChar ->
      log.info("Starting full load for alpha char {} ", alphaChar)
      fullLoadSingleAlphaChar(alphaChar, loadDate)
    }
  }

  private fun fullLoadSingleAlphaChar(
    alphaChar: Char,
    loadDate: LocalDateTime?
  ) {
    val sdrsResponse = findAllOffencesByAlphaChar(alphaChar)
    if (sdrsResponse.messageStatus.status == "ERRORED") {
      handleSdrsError(sdrsResponse, alphaChar, loadDate, FULL_LOAD)
    } else {
      val latestOfEachOffence = getLatestOfEachOffence(sdrsResponse, alphaChar)
      offenceRepository.saveAll(latestOfEachOffence.map { transform(it) })
      saveLoad(alphaChar, loadDate, SUCCESS, FULL_LOAD)
    }
  }

  private fun handleSdrsError(
    sdrsResponse: SDRSResponse,
    alphaChar: Char,
    loadDate: LocalDateTime?,
    loadType: LoadType
  ) {
    if (sdrsResponse.messageStatus.code == SDRS_99918.errorCode) {
      // SDRS-99918 indicates the absence of a cache, however it also gets thrown when there are no offences matching the alphaChar
      // e.g. no offence codes start with Q; therefore handling as a 'success' here
      log.info("SDRS-9918 thrown by SDRS service due to no cache for alpha char {} (treated as success with no offences)", alphaChar)
      saveLoad(alphaChar, loadDate, SUCCESS, loadType)
    } else {
      log.error("Request to SDRS API failed for alpha char {} ", alphaChar)
      log.error("Response details: {}", sdrsResponse)
      saveLoad(alphaChar, loadDate, FAIL, loadType)
    }
  }

  @Transactional
  fun loadOffenceUpdates() {
    val sdrsLoadStatuses = sdrsLoadStatusRepository.findAll()
    val lastLoadDateByAlphaChar = sdrsLoadStatuses.groupBy({ it.lastSuccessfulLoadDate }, { it.alphaChar.single() })
    val loadDate = LocalDateTime.now()
    lastLoadDateByAlphaChar.forEach { (lastSuccessfulLoadDate, affectedCaches) ->
      if (lastSuccessfulLoadDate == null) {
        affectedCaches.forEach {
          log.info("Cache has not been previously loaded, so attempting full load of {} in update job", it)
          fullLoadSingleAlphaChar(it, loadDate)
        }
      } else {
        val updatedCaches = getUpdatedCachesSinceLastLoadDate(lastSuccessfulLoadDate)
        val cachesToUpdate = affectedCaches intersect updatedCaches
        log.info("Caches to update are {}", cachesToUpdate)
        cachesToUpdate.forEach { alphaChar ->
          updateSingleCache(alphaChar, lastSuccessfulLoadDate, loadDate)
        }
      }
    }
  }

  private fun updateSingleCache(
    alphaChar: Char,
    lastLoadDate: LocalDateTime,
    loadDate: LocalDateTime?
  ) {
    log.info("Starting update load for alpha char {} ", alphaChar)
    val sdrsResponse = findUpdatedOffences(alphaChar, lastLoadDate)
    if (sdrsResponse.messageStatus.status == "ERRORED") {
      handleSdrsError(sdrsResponse, alphaChar, loadDate, UPDATE)
    } else {
      val latestOfEachOffence = getLatestOfEachOffence(sdrsResponse, alphaChar)
      latestOfEachOffence.forEach {
        offenceRepository.findOneByCode(it.code).ifPresentOrElse(
          { offenceToUpdate -> offenceRepository.save(transform(it, offenceToUpdate)) },
          { offenceRepository.save(transform(it)) }
        )
      }
      saveLoad(alphaChar, loadDate, SUCCESS, UPDATE)
    }
  }

  private fun getLatestOfEachOffence(
    sdrsResponse: SDRSResponse,
    alphaChar: Char
  ): List<Offence> {
    val allOffences = sdrsResponse.messageBody.gatewayOperationType.getOffenceResponse!!.offences
    log.info("Fetched {} records from SDRS for alpha char {} ", allOffences.size, alphaChar)
    val latestOfEachOffence = allOffences.groupBy { it.code }.map {
      it.value.sortedByDescending { offence -> offence.offenceStartDate }[0]
    }
    return latestOfEachOffence
  }

  private fun getUpdatedCachesSinceLastLoadDate(lastLoadDate: LocalDateTime): Set<Char> {
    val controlResults = makeControlTableRequest(lastLoadDate)
    return controlResults.messageBody.gatewayOperationType.getControlTableResponse!!.referenceDataSet
      .filter {
        it.dataSet.startsWith("offence_")
      }
      .map {
        it.dataSet.toCharArray().last()
      }.toSet()
  }

  private fun saveLoad(alphaChar: Char, loadDate: LocalDateTime?, status: LoadStatus, type: LoadType) {
    val loadStatusExisting = sdrsLoadStatusRepository.findById(alphaChar.toString())
      .orElseThrow { EntityNotFoundException("No record exists for alphaChar $alphaChar") }
    val loadStatus = if (status == SUCCESS) {
      loadStatusExisting.copy(
        status = status,
        loadType = type,
        loadDate = loadDate,
        lastSuccessfulLoadDate = loadDate
      )
    } else {
      loadStatusExisting.copy(
        status = status,
        loadType = type,
        loadDate = loadDate,
      )
    }
    sdrsLoadStatusRepository.save(loadStatus)

    val loadStatusHistory = SdrsLoadResultHistory(
      alphaChar = alphaChar.toString(),
      status = status,
      loadType = type,
      loadDate = loadDate,
    )
    sdrsLoadStatusHistoryRepository.save(loadStatusHistory)
  }

  fun makeControlTableRequest(changedDateTime: LocalDateTime): SDRSResponse {
    log.info("Making a control table request from SDRS")
    val sdrsRequest = createControlTableRequest(changedDateTime)
    return sdrsApiClient.callSDRS(sdrsRequest)
  }

  private fun createSDRSRequest(gatewayOperationTypeRequest: GatewayOperationTypeRequest, messageType: String) =
    SDRSRequest(
      messageHeader = MessageHeader(
        messageID = MessageID(
          uuid = UUID.randomUUID(),
          relatesTo = ""
        ),
        timeStamp = ZonedDateTime.now(),
        messageType = messageType,
        from = "CONSUMER_APPLICATION",
        to = "SDRS_AZURE"
      ),
      messageBody = MessageBodyRequest(
        gatewayOperationTypeRequest
      )
    )

  private fun createOffenceRequest(
    offenceCode: String? = null,
    changedDate: LocalDateTime? = null,
    alphaChar: Char? = null,
    allOffences: String = "ALL"
  ) =
    createSDRSRequest(
      GatewayOperationTypeRequest(
        getOffenceRequest = GetOffenceRequest(
          cjsCode = offenceCode,
          alphaChar = alphaChar,
          allOffences = allOffences,
          changedDate = changedDate,
        )
      ),
      "GetOffence"
    )

  private fun createControlTableRequest(changedDateTime: LocalDateTime) =
    createSDRSRequest(
      GatewayOperationTypeRequest(
        getControlTableRequest = GetControlTableRequest(
          changedDateTime = changedDateTime
        )
      ),
      "GetControlTable"
    )

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
