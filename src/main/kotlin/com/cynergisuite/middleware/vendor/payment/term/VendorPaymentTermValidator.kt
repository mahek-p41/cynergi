package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorPaymentTermValidator @Inject constructor(
   private val vendorPaymentTermRepository: VendorPaymentTermRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(vo: VendorPaymentTermValueObject){
      logger.trace("Validating Save VendorPaymentTerm {}", vo)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: VendorPaymentTermValueObject){
      logger.trace("Validating Update VendorPaymentTerm {}", vo)

      doValidation { errors ->
         val id = vo.id
//See what validation is done in Z
         if (id == null) {
            errors.add(element = ValidationError("id", NotNull("id")))
         } else if ( !vendorPaymentTermRepository.exists(id = id) ) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }
   }
}
