package com.cynergisuite.middleware.verfication

import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
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
   fun validateSave(vo: VerificationValueObject, parent: String) {
      val errors = if (verificationService.exists(customerAccount = vo.customerAccount!!)) {
         setOf(ValidationError("cust_acct", DUPLICATE, listOf(vo.customerAccount)))
      } else {
         emptySet()
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: VerificationValueObject, parent: String) {
      val errors = mutableSetOf<ValidationError>()
      val id = vo.id

      if (id == null) {
         errors.add(element = ValidationError("id", NOT_NULL, listOf("id")))
      } else {
         val existingVerification: VerificationValueObject? = verificationService.fetchById(id = id)

         if (existingVerification == null) {
            errors.add(element = ValidationError("id", NOT_FOUND, listOf(id)))
         } else if (existingVerification.customerAccount != vo.customerAccount) {
            errors.add(element = ValidationError("cust_acct", NOT_UPDATABLE, listOf(vo.customerAccount)))
         }
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors = errors)
      }
   }
}
