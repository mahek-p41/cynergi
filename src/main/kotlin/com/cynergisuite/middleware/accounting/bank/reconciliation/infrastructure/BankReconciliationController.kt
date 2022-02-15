package com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/bank-recon")
class BankReconciliationController @Inject constructor(
   private val userService: UserService,
   private val bankReconciliationService: BankReconciliationService
) {
   private val logger: Logger = LoggerFactory.getLogger(BankReconciliationController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["BankReconciliationEndpoints"], summary = "Fetch a single BankReconciliation", description = "Fetch a single BankReconciliation by ID", operationId = "bankReconciliation-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankReconciliationDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested BankReconciliation was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): BankReconciliationDTO {
      logger.info("Fetching BankReconciliation by ID {}", id)

      val user = userService.fetchUser(authentication)
      val response = bankReconciliationService.fetchById(id, user.myCompany(), httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)

      logger.debug("Fetching BankReconciliation by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["BankReconciliationEndpoints"], summary = "Fetch a list of bank reconciliations", description = "Fetch a listing of bank reconciliations", operationId = "bankReconciliation-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))])
      ]
   )
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication
   ): Page<BankReconciliationDTO> {
      val user = userService.fetchUser(authentication)
      val bankRecons = bankReconciliationService.fetchAll(user.myCompany(), pageRequest)

      if (bankRecons.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of Bank Reconciliations resulted in {}", bankRecons)

      return bankRecons
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["BankReconciliationEndpoints"], summary = "Create a single bank reconciliation", description = "Create a single bank reconciliation.", operationId = "bankReconciliation-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save BankReconciliation", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankReconciliationDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: BankReconciliationDTO,
      authentication: Authentication
   ): BankReconciliationDTO {
      logger.info("Requested Save BankReconciliation {}", dto)

      val user = userService.fetchUser(authentication)
      val response = bankReconciliationService.create(dto, user.myCompany())

      logger.debug("Requested Save BankReconciliation {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["BankReconciliationEndpoints"], summary = "Create a single bank reconciliation", description = "Create a single bank reconciliation.", operationId = "bankReconciliation-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update BankReconciliation", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankReconciliationDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested BankReconciliation was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: BankReconciliationDTO,
      authentication: Authentication
   ): BankReconciliationDTO {
      logger.info("Requested Update BankReconciliation {}", dto)

      val user = userService.fetchUser(authentication)
      val response = bankReconciliationService.update(id, dto, user.myCompany())

      logger.debug("Requested Update BankReconciliation {} resulted in {}", dto, response)

      return response
   }
}
