package com.cynergisuite.middleware.financial.statement.layout.infrastructure

import com.cynergisuite.middleware.accounting.general.ledger.end.year.EndYearProceduresDTO
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.financial.statement.layout.FinancialStatementLayoutDTO
import com.cynergisuite.middleware.financial.statement.layout.FinancialStatementService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Controller("/api/financial-statement/layout")
class FinancialStatementLayoutController @Inject constructor(
   private val financialStatementService: FinancialStatementService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(FinancialStatementLayoutController::class.java)

   @Post(processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["FinancialStatementLayoutEndpoints"], description = "Financial Statement Layout", operationId = "FinancialStatementLayout-create")
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
   fun create(
      @Body @Valid
      dto: FinancialStatementLayoutDTO,
      authentication: Authentication
   ) {
      val user = userService.fetchUser(authentication)
      logger.info("Requested create a financial statement layout {}", dto)

      financialStatementService.create(dto, user)

      logger.debug("Successfully created a financial statement layout")

   }

}
