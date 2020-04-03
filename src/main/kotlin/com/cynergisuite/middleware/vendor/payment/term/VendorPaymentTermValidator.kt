package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.localization.VendorPaymentTermDoesNotMatchDue
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

      doValidation { errors -> doSharedValidation(errors, vo) }

      return VendorPaymentTermEntity(vo = vo, company = company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, vo: VendorPaymentTermValueObject): VendorPaymentTermEntity {
      logger.trace("Validating Update VendorPaymentTerm {}", vo)

      val existing = vendorPaymentTermRepository.findOne(id) ?: throw NotFoundException(id)

      doValidation { errors -> doSharedValidation(errors, vo) }

      return VendorPaymentTermEntity(source = existing, updateWith = vo)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, vo: VendorPaymentTermValueObject) {
      when(vo.numberOfPayments) {
         1 -> if (vo.dueMonth1 == null) errors.add(ValidationError("dueMonth1", VendorPaymentTermDoesNotMatchDue(1)))
         2 -> if (vo.dueMonth2 == null) errors.add(ValidationError("dueMonth2", VendorPaymentTermDoesNotMatchDue(2)))
         3 -> if (vo.dueMonth3 == null) errors.add(ValidationError("dueMonth3", VendorPaymentTermDoesNotMatchDue(3)))
         4 -> if (vo.dueMonth4 == null) errors.add(ValidationError("dueMonth4", VendorPaymentTermDoesNotMatchDue(4)))
         5 -> if (vo.dueMonth5 == null) errors.add(ValidationError("dueMonth5", VendorPaymentTermDoesNotMatchDue(5)))
         6 -> if (vo.dueMonth6 == null) errors.add(ValidationError("dueMonth6", VendorPaymentTermDoesNotMatchDue(6)))
      }

      if(vo.dueMonth1 == null) {
         errors.add(ValidationError("dueMonth1", NotNull("dueMonth1")))
      }
      if(vo.dueDays1 == null) {
         errors.add(ValidationError("dueDays1", NotNull("dueDays1")))
      }
   }
}
