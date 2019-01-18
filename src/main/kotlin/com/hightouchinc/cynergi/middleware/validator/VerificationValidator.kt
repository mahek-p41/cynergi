package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.VerificationDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.VerificationService
import javax.inject.Singleton

@Singleton
class VerificationValidator(
   private val verificationService: VerificationService
) {

   @Throws(ValidationException::class)
   fun validateSave(dto: VerificationDto, parent: String) {
      val errors = if (verificationService.exists(customerAccount = dto.customerAccount!!)) {
         setOf(ValidationError("cust_acct", ErrorCodes.Cynergi.DUPLICATE, listOf(dto.customerAccount)))
      } else {
         emptySet()
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(dto: VerificationDto, parent: String) {
      val errors = mutableSetOf<ValidationError>()
      val id = dto.id

      if (id == null) {
         errors.add(element = ValidationError("id", ErrorCodes.Validation.NOT_NULL, listOf("id")))
      } else {
         val existingVerification: VerificationDto? = verificationService.fetchById(id = id)

         if (existingVerification == null) {
            errors.add(element = ValidationError("id", ErrorCodes.System.NOT_FOUND, listOf(id)))
         } else if (existingVerification.customerAccount != dto.customerAccount) {
            errors.add(element = ValidationError("cust_acct", ErrorCodes.Cynergi.NOT_UPDATABLE, listOf(dto.customerAccount)))
         }
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors = errors)
      }
   }
}
