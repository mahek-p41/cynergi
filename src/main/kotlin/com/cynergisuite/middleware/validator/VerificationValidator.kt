package com.cynergisuite.middleware.validator

import com.cynergisuite.middleware.entity.VerificationDto
import com.cynergisuite.middleware.exception.ValidationError
import com.cynergisuite.middleware.exception.ValidationException
import com.cynergisuite.middleware.service.VerificationService
import com.cynergisuite.middleware.localization.MessageCodes.Cynergi.DUPLICATE
import com.cynergisuite.middleware.localization.MessageCodes.Cynergi.NOT_UPDATABLE
import com.cynergisuite.middleware.localization.MessageCodes.System.NOT_FOUND
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import javax.inject.Singleton

@Singleton
class VerificationValidator(
   private val verificationService: VerificationService
) {

   @Throws(ValidationException::class)
   fun validateSave(dto: VerificationDto, parent: String) {
      val errors = if (verificationService.exists(customerAccount = dto.customerAccount!!)) {
         setOf(ValidationError("cust_acct", DUPLICATE, listOf(dto.customerAccount)))
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
         errors.add(element = ValidationError("id", NOT_NULL, listOf("id")))
      } else {
         val existingVerification: VerificationDto? = verificationService.fetchById(id = id)

         if (existingVerification == null) {
            errors.add(element = ValidationError("id", NOT_FOUND, listOf(id)))
         } else if (existingVerification.customerAccount != dto.customerAccount) {
            errors.add(element = ValidationError("cust_acct", NOT_UPDATABLE, listOf(dto.customerAccount)))
         }
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors = errors)
      }
   }
}
