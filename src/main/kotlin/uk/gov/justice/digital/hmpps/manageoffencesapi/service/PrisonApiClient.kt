package uk.gov.justice.digital.hmpps.manageoffencesapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.PrisonApiHoCode
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.PrisonApiOffence
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.PrisonApiStatute
import uk.gov.justice.digital.hmpps.manageoffencesapi.model.RestResponsePage

@Service
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {
  private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
  private val log = LoggerFactory.getLogger(this::class.java)

  fun findByOffenceCodeStartsWith(offenceCode: String, pageNumber: Int): RestResponsePage<PrisonApiOffence> {
    log.info("Fetching all offences from prison-api for page number $pageNumber")
    return webClient.get()
      .uri("/api/offences/code/$offenceCode?page=$pageNumber&size=1000&sort=code,ASC")
      .retrieve()
      .bodyToMono(typeReference<RestResponsePage<PrisonApiOffence>>())
      .block()!!
  }

  fun createHomeOfficeCodes(prisonApiHoCode: List<PrisonApiHoCode>) {
    log.info("Making prison-api call to create home office offence codes")
    webClient.post()
      .uri("/api/offences/ho-code")
      .bodyValue(prisonApiHoCode)
      .retrieve()
      .toBodilessEntity()
      .block()
  }

  fun createStatutes(prisonApiStatute: List<PrisonApiStatute>) {
    log.info("Making prison-api call to create statutes")
    webClient.post()
      .uri("/api/offences/statute")
      .bodyValue(prisonApiStatute)
      .retrieve()
      .toBodilessEntity()
      .block()
  }

  fun createOffences(prisonApiOffence: List<PrisonApiOffence>) {
    log.info("Making prison-api call to create offences")
    webClient.post()
      .uri("/api/offences/offence")
      .bodyValue(prisonApiOffence)
      .retrieve()
      .toBodilessEntity()
      .block()
  }

  fun updateOffences(updatedNomisOffences: List<PrisonApiOffence>) {
    log.info("Making prison-api call to update offences")
    webClient.put()
      .uri("/api/offences/offence")
      .bodyValue(updatedNomisOffences)
      .retrieve()
      .toBodilessEntity()
      .block()
  }
}
