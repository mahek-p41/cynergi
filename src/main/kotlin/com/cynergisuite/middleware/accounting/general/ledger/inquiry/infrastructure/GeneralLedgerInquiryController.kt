package com.cynergisuite.middleware.accounting.general.ledger.inquiry.infrastructure

import com.cynergisuite.domain.GeneralLedgerInquiryFilterRequest
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerInquiryDTO
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerInquiryService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/general-ledger/inquiry")
class GeneralLedgerInquiryController @Inject constructor(
   private val generalLedgerInquiryService: GeneralLedgerInquiryService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerInquiryController::class.java)

   @Get(uri = "{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerInquiryEndpoints"], summary = "Fetch a single GeneralLedgerInquiryDTO", description = "Fetch a single GeneralLedgerInquiryDTO that is associated with the logged-in user's company", operationId = "generalLedgerInquiry-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerInquiryDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerInquiry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(name = "filterRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerInquiryFilterRequest,
      authentication: Authentication
   ): GeneralLedgerInquiryDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Fetching GeneralLedgerInquiry")

      val response = generalLedgerInquiryService.fetchOne(userCompany, filterRequest) ?: throw NotFoundException("")

      logger.debug("Fetching GeneralLedgerInquiry by {} resulted in", response)

      return response
   }

}
