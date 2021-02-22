package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.bank.BankDTO
import com.cynergisuite.middleware.accounting.bank.BankService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/bank")
class BankController @Inject constructor(
   private val userService: UserService,
   private val bankService: BankService
) {
   private val logger: Logger = LoggerFactory.getLogger(BankController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["BankEndpoints"], summary = "Fetch a single Bank", description = "Fetch a single Bank by ID", operationId = "bank-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested Bank was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): BankDTO {
      logger.info("Fetching Bank by ID {}", id)

      val user = userService.findUser(authentication)
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
      val user = userService.findUser(authentication)
      val banks = bankService.fetchAll(user.myCompany(), pageRequest)

      if (banks.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of Bank Currency Codes resulted in {}", banks)

      return banks
   }

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
      authentication: Authentication
   ): BankDTO {
      logger.info("Requested Save Bank {}", dto)

      val user = userService.findUser(authentication)
      val response = bankService.create(dto, user.myCompany())

      logger.debug("Requested Save Bank {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9]+}", processes = [APPLICATION_JSON])
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
      @QueryValue("id") id: Long,
      @Body @Valid
      dto: BankDTO,
      authentication: Authentication
   ): BankDTO {
      logger.info("Requested Update Bank {}", dto)

      val user = userService.findUser(authentication)
      val response = bankService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Bank {} resulted in {}", dto, response)

      return response
   }

   @Delete(uri = "/{id:[0-9]+}", processes = [APPLICATION_JSON])
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
      @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete bank", authentication)

      val user = userService.findUser(authentication)

      return bankService.delete(id, user.myCompany())
   }
}
