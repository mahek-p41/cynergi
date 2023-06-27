package com.cynergisuite.middleware.accounting.general.ledger.end.year.infrastructure

import com.cynergisuite.middleware.accounting.general.ledger.end.year.EndYearProceduresDTO
import com.cynergisuite.middleware.accounting.general.ledger.end.year.GeneralLedgerProcedureService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Secured(SecurityRule.IS_AUTHENTICATED)
@AreaControl("GL")
@Controller("/api/general-ledger/procedure")
class GeneralLedgerProcedureController @Inject constructor(
   private val generalLedgerProcedureService: GeneralLedgerProcedureService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerProcedureController::class.java)

   @Post(uri = "end-year", processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerProcedureEndpoints"], summary = "Close current GL year", description = "Close current GL year", operationId = "GeneralLedgerProcedure-endCurrentYear")
   @ApiResponses(
      value = [
          ApiResponse(
              responseCode = "200",
              content = [Content(
                  mediaType = MediaType.APPLICATION_JSON,
                  schema = Schema(implementation = EndYearProceduresDTO::class)
              )]
          ),
          ApiResponse(
              responseCode = "400",
              description = "If one of the required properties in the payload is missing"
          ),
          ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun endCurrentGLYear(
      @Body @Valid
      dto: EndYearProceduresDTO,
      authentication: Authentication
   ) {
      val user = userService.fetchUser(authentication)
      logger.info("Requested close current GL year {}", dto)

      val response = generalLedgerProcedureService.endCurrentYear(dto, user)

      logger.debug("Requested close current GL year {} resulted in {}", dto, response)

   }

}
