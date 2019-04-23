package com.cynergisuite.middleware.controller

import com.cynergisuite.middleware.entity.VerificationDto
import com.cynergisuite.middleware.exception.NotFoundException
import com.cynergisuite.middleware.exception.ValidationException
import com.cynergisuite.middleware.service.VerificationService
import com.cynergisuite.middleware.validator.VerificationValidator
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
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
@Validated
@Controller("/api/verifications/{companyId}/")
class VerificationController @Inject constructor(
   private val verificationService: VerificationService,
   private val verificationValidator: VerificationValidator
) {
   private val logger: Logger = LoggerFactory.getLogger(com.cynergisuite.middleware.controller.VerificationController::class.java)

   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): VerificationDto {
      logger.info("Fetching Verification by {}", id)

      val response = verificationService.fetchById(id = id) ?: throw NotFoundException(id)

      logger.debug("Fetching Verification by {} resulted in", id, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(value = "/account/{customerAccount}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("companyId") companyId: String,
      @QueryValue("customerAccount") customerAccount: String
   ): VerificationDto {
      logger.info("Fetching Verification by company: {}, customer account {}", companyId, customerAccount)

      val response = verificationService.fetchByCustomerAccount(customerAccount = customerAccount) ?: throw NotFoundException(customerAccount)

      logger.debug("Fetching Verification by company: {}, customer account {} resulted in {}", companyId, customerAccount, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun save(
      @QueryValue("companyId") companyId: String,
      @Valid @Body dto: VerificationDto
   ): VerificationDto {
      logger.info("Requested Save Validation {} with company: {}", dto, companyId)

      verificationValidator.validateSave(dto = dto, parent = companyId)

      val response = verificationService.create(dto = dto, parent = companyId)

      logger.info("Requested Save Validation {} with company: {} resulted in", dto, companyId, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun update(
      @QueryValue("companyId") companyId: String,
      @Valid @Body dto: VerificationDto
   ): VerificationDto {
      logger.info("Requested Update Validation {} with company: {}", dto, companyId)

      verificationValidator.validateUpdate(dto = dto, parent = companyId)

      val response = verificationService.update(dto = dto, parent = companyId)

      logger.info("Requested Update Validation {} with company: {} resulted in", dto, companyId, response)

      return response
   }
}
