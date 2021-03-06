package uk.gov.justice.digital.hmpps.manageoffencesapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class SDRSApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8332
  }

  fun stubGetAllOffencesForA() {
    stubFor(
      post("/cld_StandingDataReferenceService/service/sdrs/sdrs/sdrsApi")
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.MessageType == 'GetOffence')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.From == 'CONSUMER_APPLICATION')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.To == 'SDRS_AZURE')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AllOffences == 'ALL')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AlphaChar == 'A')]"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """ {
                    "MessageBody": {
                      "GatewayOperationType": {
                        "GetOffenceResponse": {
                          "Offence": [
                            {
                              "OffenceRevisionId": 410082,
                              "OffenceStartDate": "2013-03-01",
						                  "OffenceEndDate": "2013-03-02",
                              "Description": "EMPTY TEMPLATE FOR USE WHERE A STANDARD OFFENCE WORDING IS NOT AVAILABLE",
                              "MOJStatsCode": "195/99",
                              "code": "XX99001"
                            },
                            {
                              "OffenceRevisionId": 354116,
						                  "OffenceStartDate": "2005-09-02",
						                  "OffenceEndDate": "2005-09-03",
                              "Description": "EMPTY TEMPLATE FOR USE WHERE A STANDARD OFFENCE WORDING IS NOT AVAILABLE",
                              "MOJStatsCode": "195/99",
                              "code": "XX99001"
                            }
                          ]
                        }
		                  }
	                  },
                    "MessageHeader": {
                      "MessageID": {
                        "UUID": "7717d82c-9cc2-4983-acf1-0d42770e88bd",
                        "RelatesTo": "df2200e6-241c-4642-b391-3d53299185cd"
                      },
                      "TimeStamp": "2022-03-01T15:00:00Z",
                      "MessageType": "getOffence",
                      "From": "SDRS_AZURE",
                      "To": "CONSUMER_APPLICATION"
                    },
                    "MessageStatus": {
                      "status": "SUCCESS",
                      "code": " ",
                      "reason": " ",
                      "detail": " "
                    }
                  }
              """.trimIndent()
            )
        )
    )
  }

  fun stubGetChangedOffencesForA() {
    stubFor(
      post("/cld_StandingDataReferenceService/service/sdrs/sdrs/sdrsApi")
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.MessageType == 'GetOffence')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.From == 'CONSUMER_APPLICATION')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.To == 'SDRS_AZURE')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AllOffences == 'ALL')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AlphaChar == 'A')]"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """ {
                    "MessageBody": {
                      "GatewayOperationType": {
                        "GetOffenceResponse": {
                          "Offence": [
                            {
                              "OffenceRevisionId": 99990,
                              "OffenceStartDate": "2013-01-01",
						                  "OffenceEndDate": "2013-12-31",
                              "Description": "EMPTY TEMPLATE FOR USE WHERE A STANDARD OFFENCE WORDING IS NOT AVAILABLE",
                              "code": "XX99001"
                            },
                            {
                              "OffenceRevisionId": 99991,
                              "OffenceStartDate": "2014-01-01",
						                  "OffenceEndDate": "",
                              "Description": "EMPTY TEMPLATE FOR USE WHERE A STANDARD OFFENCE WORDING IS NOT AVAILABLE",
                              "code": "XX99001"
                            }
                          ]
                        }
		                  }
	                  },
                    "MessageHeader": {
                      "MessageID": {
                        "UUID": "7717d82c-9cc2-4983-acf1-0d42770e88bd",
                        "RelatesTo": "df2200e6-241c-4642-b391-3d53299185cd"
                      },
                      "TimeStamp": "2022-03-01T15:00:00Z",
                      "MessageType": "getOffence",
                      "From": "SDRS_AZURE",
                      "To": "CONSUMER_APPLICATION"
                    },
                    "MessageStatus": {
                      "status": "SUCCESS",
                      "code": " ",
                      "reason": " ",
                      "detail": " "
                    }
                  }
              """.trimIndent()
            )
        )
    )
  }

  fun stubGetChangedOffencesForAHasBadJson() {
    stubFor(
      post("/cld_StandingDataReferenceService/service/sdrs/sdrs/sdrsApi")
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.MessageType == 'GetOffence')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.From == 'CONSUMER_APPLICATION')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.To == 'SDRS_AZURE')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AllOffences == 'ALL')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AlphaChar == 'A')]"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """ {
                    "MessageBody": {
                      "GatewayOperationType": {
                        "GetOffenceResponse": {
                          "Offence": [
                            {
                              "OffenceRevisionId": 99990,
                              "OffenceStartDate": "2013-01-01Z",
						                  "OffenceEndDate": "2013-12-31",
                              "Description": "EMPTY TEMPLATE FOR USE WHERE A STANDARD OFFENCE WORDING IS NOT AVAILABLE",
                              "code": "XX99001"
                            },
                            {
                              "OffenceRevisionId": 99991,
                              "OffenceStartDate": "2014-01-01Z",
						                  "OffenceEndDate": "",
                              "Description": "EMPTY TEMPLATE FOR USE WHERE A STANDARD OFFENCE WORDING IS NOT AVAILABLE",
                              "code": "XX99001"
                            }
                          ]
                        }
		                  }
	                  },
                    "MessageHeader": {
                      "MessageID": {
                        "UUID": "7717d82c-9cc2-4983-acf1-0d42770e88bd",
                        "RelatesTo": "df2200e6-241c-4642-b391-3d53299185cd"
                      },
                      "TimeStamp": "2022-03-01T15:00:00Z",
                      "MessageType": "getOffence",
                      "From": "SDRS_AZURE",
                      "To": "CONSUMER_APPLICATION"
                    },
                    "MessageStatus": {
                      "status": "SUCCESS",
                      "code": " ",
                      "reason": " ",
                      "detail": " "
                    }
                  }
              """.trimIndent()
            )
        )
    )
  }

  fun stubGetAllOffencesReturnEmptyArray() {
    stubFor(
      post("/cld_StandingDataReferenceService/service/sdrs/sdrs/sdrsApi")
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.MessageType == 'GetOffence')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.From == 'CONSUMER_APPLICATION')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.To == 'SDRS_AZURE')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AllOffences == 'ALL')]"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """ {
                    "MessageBody": {
                      "GatewayOperationType": {
                        "GetOffenceResponse": {
                          "Offence": []
                        }
		                  }
	                  },
                    "MessageHeader": {
                      "MessageID": {
                        "UUID": "7717d82c-9cc2-4983-acf1-0d42770e88bd",
                        "RelatesTo": "df2200e6-241c-4642-b391-3d53299185cd"
                      },
                      "TimeStamp": "2022-03-01T15:00:00Z",
                      "MessageType": "getOffence",
                      "From": "SDRS_AZURE",
                      "To": "CONSUMER_APPLICATION"
                    },
                    "MessageStatus": {
                      "status": "SUCCESS",
                      "code": " ",
                      "reason": " ",
                      "detail": " "
                    }
                  }
              """.trimIndent()
            )
        )
    )
  }

  fun stubControlTableRequest() {
    stubFor(
      post("/cld_StandingDataReferenceService/service/sdrs/sdrs/sdrsApi")
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.MessageType == 'GetControlTable')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.From == 'CONSUMER_APPLICATION')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.To == 'SDRS_AZURE')]"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """ {
                    "MessageBody":{
                      "GatewayOperationType":{
                        "GetControlTableResponse":{
                          "ReferenceDataSet":[
                            {
                              "DataSet":"offence_A",
                              "LastUpdate":"2022-04-05T09:17:19.823"
                            },
                            {
                              "DataSet":"offence_B",
                              "LastUpdate":"2022-04-05T09:16:58.595"
                            }
                          ]
                        }
                      }
                    },                    
                    "MessageStatus": {
                      "status": "SUCCESS"
                    }
                  }
              """.trimIndent()
            )
        )
    )
  }

  fun stubGetAllOffencesForQHasNoCache() {
    stubFor(
      post("/cld_StandingDataReferenceService/service/sdrs/sdrs/sdrsApi")
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.MessageType == 'GetOffence')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.From == 'CONSUMER_APPLICATION')]"))
        .withRequestBody(matchingJsonPath("$.MessageHeader[?(@.To == 'SDRS_AZURE')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AllOffences == 'ALL')]"))
        .withRequestBody(matchingJsonPath("$.MessageBody[?(@.GatewayOperationType.GetOffenceRequest.AlphaChar == 'Q')]"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """ {
                    "MessageBody": {
                      "GatewayOperationType": {
                        "Acknowledgement": {
                          "Ack": {
                            "MessageStatus": "ERRORED",
                            "MessageComment": "",
                            "TimeStamp": 1650459143863
                          }
                        }
		                  }
	                  },
                    "MessageHeader": {
                      "MessageID": {
                        "UUID": "7717d82c-9cc2-4983-acf1-0d42770e88bd",
                        "RelatesTo": "df2200e6-241c-4642-b391-3d53299185cd"
                      },
                      "TimeStamp": "2022-03-01T15:00:00Z",
                      "MessageType": "getOffence",
                      "From": "SDRS_AZURE",
                      "To": "CONSUMER_APPLICATION"
                    },
                    "MessageStatus": {
                      "status": "ERRORED",
                      "code": "SDRS-99918",
                      "reason": " ",
                      "detail": " "
                    }
                  }
              """.trimIndent()
            )
        )
    )
  }
}
