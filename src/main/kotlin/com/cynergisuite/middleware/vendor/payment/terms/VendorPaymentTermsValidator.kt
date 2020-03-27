package com.cynergisuite.middleware.vendor.payment.terms

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.vendor.payment.terms.infrastructure.VendorPaymentTermsRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorPaymentTermsValidator @Inject constructor(
   private val vendorPaymentTermsRepository: VendorPaymentTermsRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermsValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(vo: VendorPaymentTermsValueObject){
      logger.trace("Validating Save VendorPaymentTerms {}", vo)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: VendorPaymentTermsValueObject){
      logger.trace("Validating Update VendorPaymentTerms {}", vo)

      doValidation { errors ->
         val id = vo.id

         if (id == null) {
            errors.add(element = ValidationError("id", NotNull("id")))
         } else if ( !vendorPaymentTermsRepository.exists(id = id) ) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }
   }
}
