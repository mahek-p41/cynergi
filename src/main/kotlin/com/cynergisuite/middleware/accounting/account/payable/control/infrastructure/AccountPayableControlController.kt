package com.cynergisuite.middleware.accounting.account.payable.control.infrastructure

import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlDTO
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/account/payable/control")
class AccountPayableControlController @Inject constructor(
   private val accountPayableControlService: AccountPayableControlService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableControlController::class.java)

   @Get(produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableControlEndpoints"], summary = "Fetch a single AccountPayableControlDTO", description = "Fetch a single AccountPayableControlDTO that is associated with the logged-in user's company", operationId = "accountPayableControl-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableControlDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableControl was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      authentication: Authentication
   ): AccountPayableControlDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Fetching AccountPayableControl by {}", userCompany)

      val response = accountPayableControlService.fetchOne(userCompany) ?: throw NotFoundException("Account payable control record of the company")

      logger.debug("Fetching AccountPayableControl by {} resulted in", userCompany, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableControlEndpoints"], summary = "Create an AccountPayableControlEntity", description = "Create an AccountPayableControlEntity", operationId = "accountPayableControl-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableControlDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   @AccessControl
   fun create(
      @Body dto: AccountPayableControlDTO,
      authentication: Authentication
   ): AccountPayableControlDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create AccountPayableControl {}", dto)

      val response = accountPayableControlService.create(dto, userCompany)

      logger.debug("Requested Create AccountPayableControl {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableControlEndpoints"], summary = "Update an AccountPayableControlEntity", description = "Update an AccountPayableControlEntity from a body of AccountPayableControlDTO", operationId = "accountPayableControl-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update AccountPayableControl", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableControlDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableControl was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   @AccessControl
   fun update(
      @QueryValue("id") id: Long,
      @Body dto: AccountPayableControlDTO,
      authentication: Authentication
   ): AccountPayableControlDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update AccountPayableControl {}", dto)

      val response = accountPayableControlService.update(id, dto, userCompany)

      logger.debug("Requested Update AccountPayableControl {} resulted in {}", dto, response)

      return response
   }
}
