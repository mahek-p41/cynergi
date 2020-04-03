package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
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
   fun validateCreate(vo: VendorPaymentTermValueObject, company: Company): VendorPaymentTermEntity {
      logger.trace("Validating Save VendorPaymentTerm {}", vo)

      return VendorPaymentTermEntity(vo = vo, company = company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, vo: VendorPaymentTermValueObject): VendorPaymentTermEntity {
      logger.trace("Validating Update VendorPaymentTerm {}", vo)

      val existing = vendorPaymentTermRepository.findOne(id) ?: throw NotFoundException(id)

      doValidation { errors ->
         //TODO additional validation

      }

      return VendorPaymentTermEntity(source = existing, updateWith = vo)
   }
}
