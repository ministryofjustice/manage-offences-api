package uk.gov.justice.digital.hmpps.manageoffencesapi.model.external.sdrs

import com.fasterxml.jackson.databind.PropertyNamingStrategies.UpperCamelCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(UpperCamelCaseStrategy::class)
data class GatewayOperationTypeResponse(
  val getOffenceResponse: GetOffenceResponse? = null,
  val getControlTableResponse: GetControlTableResponse? = null,

)
