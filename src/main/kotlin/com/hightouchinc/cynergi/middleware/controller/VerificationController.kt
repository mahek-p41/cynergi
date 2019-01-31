package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.entity.VerificationDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.VerificationService
import com.hightouchinc.cynergi.middleware.validator.VerificationValidator
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
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
   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): VerificationDto {
      return verificationService.fetchById(id = id) ?: throw NotFoundException(id)
   }

   @Throws(NotFoundException::class)
   @Get(value = "/account/{customerAccount}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("companyId") companyId: String,
      @QueryValue("customerAccount") customerAccount: String
   ): VerificationDto {
      return verificationService.fetchByCustomerAccount(customerAccount = customerAccount) ?: throw NotFoundException(customerAccount)
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun save(
      @QueryValue("companyId") companyId: String,
      @Valid @Body dto: VerificationDto
   ): VerificationDto {
      verificationValidator.validateSave(dto = dto, parent = companyId)

      return verificationService.create(dto = dto, parent = companyId)
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun update(
      @QueryValue("companyId") companyId: String,
      @Valid @Body dto: VerificationDto
   ): VerificationDto {
      verificationValidator.validateUpdate(dto = dto, parent = companyId)

      return verificationService.update(dto = dto, parent = companyId)
   }
}
