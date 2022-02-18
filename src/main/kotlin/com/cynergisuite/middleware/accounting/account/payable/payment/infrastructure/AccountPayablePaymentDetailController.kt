package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
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
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/account-payable/payment/detail")
class AccountPayablePaymentDetailController @Inject constructor(
   private val apPaymentDetailService: AccountPayablePaymentDetailService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayablePaymentDetailController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayablePaymentDetailEndpoints"], summary = "Fetch a single Account Payable Payment", description = "Fetch a single AAccount Payable Payment Detail by its system generated primary key", operationId = "AccountPayablePaymentDetail-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayablePaymentDetailDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested AAccount Payable Payment Detail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayablePaymentDetailDTO {
      logger.info("Fetching AAccount Payable Payment Detail by {}", id)

      val user = userService.fetchUser(authentication)
      val response = apPaymentDetailService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching AAccount Payable Payment Detail by {} resulted in", id, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayablePaymentDetailEndpoints"], summary = "Create a single Account Payable Payment", description = "Create a single Account Payable Payment Detail", operationId = "AccountPayablePaymentDetail-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayablePaymentDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The AAccount Payable Payment Detail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: AccountPayablePaymentDetailDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayablePaymentDetailDTO {
      logger.debug("Requested Create Account Payable Payment Detail {}", dto)

      val user = userService.fetchUser(authentication)
      val response = apPaymentDetailService.create(dto, user.myCompany())

      logger.debug("Requested Create Account Payable Payment Detail {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayablePaymentDetailEndpoints"], summary = "Update a single Account Payable Payment", description = "Update a single Account Payable Payment Detail", operationId = "AccountPayablePaymentDetail-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayablePaymentDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested AAccount Payable Payment Detail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the AAccount Payable Payment Detail being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: AccountPayablePaymentDetailDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayablePaymentDetailDTO {
      logger.info("Requested Update Account Payable Payment Detail {}", dto)

      val user = userService.fetchUser(authentication)
      val response = apPaymentDetailService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Account Payable Payment Detail {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["AccountPayablePaymentDetailEndpoints"], summary = "Delete a single AccountPayablePaymentDetail", description = "Delete a single Account Payable Payment Detail", operationId = "accountPayablePaymentDetail-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If SourceCode was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayablePaymentDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete Account Payable Payment Detail", authentication)

      val user = userService.fetchUser(authentication)

      return apPaymentDetailService.delete(id, user.myCompany())
   }
}
