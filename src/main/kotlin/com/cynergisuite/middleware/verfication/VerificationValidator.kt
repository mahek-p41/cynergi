package com.cynergisuite.middleware.verfication

import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotUpdatable
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import javax.inject.Singleton

@Singleton
class VerificationValidator(
   private val verificationService: VerificationService
) {

   @Throws(ValidationException::class)
   fun validateSave(vo: VerificationValueObject, parent: String) {
      val errors = if (verificationService.exists(customerAccount = vo.customerAccount!!)) {
         setOf(ValidationError("cust_acct", Duplicate(vo.customerAccount)))
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
         errors.add(element = ValidationError("id", NotNull("id")))
      } else {
         val existingVerification: VerificationValueObject? = verificationService.fetchById(id = id)

         if (existingVerification == null) {
            errors.add(element = ValidationError("id", NotFound(id)))
         } else if (existingVerification.customerAccount != vo.customerAccount) {
            errors.add(element = ValidationError("cust_acct", NotUpdatable(vo.customerAccount)))
         }
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors = errors)
      }
   }
}
