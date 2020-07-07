package com.cynergisuite.middleware.verfication.infrastructure

import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.verfication.VerificationService
import com.cynergisuite.middleware.verfication.VerificationValidator
import com.cynergisuite.middleware.verfication.VerificationValueObject
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid

/**
 * Defines the primary CRUD controller for the verification process
 *
 * @param verificationService defines that the [VerificationService] instance should be injected by the container
 * @param verificationValidator defines that the [VerificationValidator] instance should be injected by the container
 */
@Secured(IS_ANONYMOUS)
@Validated
@Controller("/api/verifications/{companyId}/")
class VerificationController @Inject constructor(
   private val verificationService: VerificationService,
   private val verificationValidator: VerificationValidator
) {
   private val logger: Logger = LoggerFactory.getLogger(VerificationController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VerificationEndpoints"], summary = "Fetch a single Notification", description = "Fetch a single Notification by it's system generated primary key", operationId = "verification-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "The Verification was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VerificationValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested Verification was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(name = "id", description = "The Verification ID to lookup", required = true, `in` = ParameterIn.PATH) @QueryValue("id") id: Long
   ): VerificationValueObject {
      logger.trace("Fetching Verification by {}", id)

      val response = verificationService.fetchById(id = id) ?: throw NotFoundException(id)

      logger.trace("Fetching Verification by {} resulted in", id, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(value = "/account/{customerAccount}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VerificationEndpoints"], summary = "Fetch a single Notification", description = "Fetch a single Notification by it's system generated primary key", operationId = "verification-fetchOne-company-customerAccount")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "The Verification was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VerificationValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested Verification was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(name = "id", description = "The Company ID that a Verification is to be to looked up by", required = true, `in` = ParameterIn.PATH) @QueryValue("companyId") companyId: String,
      @Parameter(name = "id", description = "The customer account number that a Verification is to be looked up by", required = true, `in` = ParameterIn.PATH) @QueryValue("customerAccount") customerAccount: String
   ): VerificationValueObject {
      logger.trace("Fetching Verification by company: {}, customer account {}", companyId, customerAccount)

      val response = verificationService.fetchByCustomerAccount(customerAccount = customerAccount) ?: throw NotFoundException(customerAccount)

      logger.trace("Fetching Verification by company: {}, customer account {} resulted in {}", companyId, customerAccount, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class)
   @Operation(tags = ["VerificationEndpoints"], summary = "Fetch a single Notification", description = "Fetch a single Notification by it's system generated primary key", operationId = "verification-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "The Verification was created successfully", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VerificationValueObject::class))]),
      ApiResponse(responseCode = "400", description = "The requested Verification to be created was invalid"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @QueryValue("companyId") companyId: String,
      @Valid @Body dto: VerificationValueObject
   ): VerificationValueObject {
      logger.trace("Requested Create Validation {} with company: {}", dto, companyId)

      verificationValidator.validateCreate(vo = dto, parent = companyId)

      val response = verificationService.create(dto = dto, parent = companyId)

      logger.trace("Requested Create Validation {} with company: {} resulted in", dto, companyId, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class)
   @Operation(tags = ["VerificationEndpoints"], summary = "Fetch a single Notification", description = "Fetch a single Notification by it's system generated primary key", operationId = "verification-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "The Verification was created successfully", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VerificationValueObject::class))]),
      ApiResponse(responseCode = "400", description = "The requested update was invalid"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @QueryValue("companyId") companyId: String,
      @Valid @Body dto: VerificationValueObject
   ): VerificationValueObject {
      logger.trace("Requested Update Validation {} with company: {}", dto, companyId)

      verificationValidator.validateUpdate(vo = dto, parent = companyId)

      val response = verificationService.update(dto = dto, parent = companyId)

      logger.trace("Requested Update Validation {} with company: {} resulted in", dto, companyId, response)

      return response
   }
}
