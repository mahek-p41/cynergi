package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.common.exceptions.NotFoundException
import com.cynergisuite.common.exceptions.PageOutOfBoundsException
import com.cynergisuite.common.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
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
@Controller("/api/accounting/bank")
class BankController @Inject constructor(
   private val userService: UserService,
   private val bankService: BankService
) {
   private val logger: Logger = LoggerFactory.getLogger(BankController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["BankEndpoints"], summary = "Fetch a single Bank", description = "Fetch a single Bank by ID", operationId = "bank-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested Bank was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): BankDTO {
      logger.info("Fetching Bank by ID {}", id)

      val user = userService.fetchUser(authentication)
      val response = bankService.fetchById(id, user.myCompany(), httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)

      logger.debug("Fetching Bank by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["BankEndpoints"], summary = "Fetch a list of banks", description = "Fetch a listing of banks", operationId = "bank-fetchAll")
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
   ): Page<BankDTO> {
      val user = userService.fetchUser(authentication)
      val banks = bankService.fetchAll(user.myCompany(), pageRequest)

      if (banks.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of Bank Currency Codes resulted in {}", banks)

      return banks
   }

   @Secured("MCFBANKADD")
   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["BankEndpoints"], summary = "Create a single bank", description = "Create a single bank.", operationId = "bank-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Bank", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun save(
      @Body @Valid
      dto: BankDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): BankDTO {
      logger.info("Requested Save Bank {}", dto)

      val user = userService.fetchUser(authentication)
      val response = bankService.create(dto, user.myCompany(), httpRequest.findLocaleWithDefault())

      logger.debug("Requested Save Bank {} resulted in {}", dto, response)

      return response
   }

   @Secured("MCFBANKCHG")
   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["BankEndpoints"], summary = "Create a single bank", description = "Create a single bank.", operationId = "bank-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Bank", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested Bank was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: BankDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): BankDTO {
      logger.info("Requested Update Bank {}", dto)

      val user = userService.fetchUser(authentication)
      val response = bankService.update(id, dto, user.myCompany(), httpRequest.findLocaleWithDefault())

      logger.debug("Requested Update Bank {} resulted in {}", dto, response)

      return response
   }

   @Secured("MCFBANKDEL")
   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @AccessControl
   @Operation(tags = ["BankEndpoints"], summary = "Delete a bank", description = "Delete a single bank", operationId = "bank-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the bank was able to be deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "409", description = "If the bank is still referenced from other tables"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete bank", authentication)

      val user = userService.fetchUser(authentication)

      return bankService.delete(id, user.myCompany(), httpRequest.findLocaleWithDefault())
   }

   @Secured("MCFBANKBALANCE")
   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}/balance", produces = [APPLICATION_JSON])
   @Operation(tags = ["BankEndpoints"], summary = "Fetch a single Bank balance", description = "Fetch a single Bank balance by ID", operationId = "bank-fetchBalance")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Bank was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchBalance(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Float {
      logger.info("Fetching Bank balance by ID {}", id)

      val user = userService.fetchUser(authentication)
      val response = bankService.fetchBalance(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Bank balance by ID {} resulted in {}", id, response)

      return response
   }
}
