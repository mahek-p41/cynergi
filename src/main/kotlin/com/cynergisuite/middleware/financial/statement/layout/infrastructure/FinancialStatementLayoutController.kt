package com.cynergisuite.middleware.financial.statement.layout.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.end.year.EndYearProceduresDTO
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.financial.statement.layout.FinancialStatementLayoutDTO
import com.cynergisuite.middleware.financial.statement.layout.FinancialStatementService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
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

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["FinancialStatementLayoutEndpoints"], summary = "Fetch a listing of Financial Statement Layouts", description = "Fetch a paginated listing of Financial Statement Layouts", operationId = "FinancialStatementLayout-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The request was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<FinancialStatementLayoutDTO> {
      logger.info("Fetching staging deposits {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val list = financialStatementService.fetchAll(user, pageRequest)

      return list
   }

}
