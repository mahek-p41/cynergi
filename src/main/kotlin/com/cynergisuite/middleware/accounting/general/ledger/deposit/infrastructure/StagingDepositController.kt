package com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositDTO
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
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
@AreaControl("GL")
@Controller("/api/general-ledger/deposit")
class StagingDepositController @Inject constructor(
   private val userService: UserService,
   private val stagingDepositService: StagingDepositService
) {
   private val logger: Logger = LoggerFactory.getLogger(StagingDepositController::class.java)

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["StagingDepositEndpoints"], summary = "Fetch a listing of Staging Deposits", description = "Fetch a paginated listing of Staging Deposits", operationId = "StagingDeposits-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Staging Deposits was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StagingDepositPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<StagingDepositDTO> {
      logger.info("Fetching staging deposits {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = stagingDepositService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }
}
