package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.extensions.equalTo
import com.cynergisuite.extensions.sum
import com.cynergisuite.extensions.toFixed
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.MustBeInRangeOf
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.localization.NotUpdatable
import com.cynergisuite.middleware.localization.VendorPaymentTermDuePercentDoesNotAddUp
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorPaymentTermValidator @Inject constructor(
   private val vendorPaymentTermRepository: VendorPaymentTermRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermValidator::class.java)

   //TODO Eventually it will be possible to delete a Vendor Payment Term. At that time, a VPT cannot
   //TODO be deleted if it is the paymentTerms on a Vendor record.

   @Throws(ValidationException::class)
   fun validateCreate(vo: VendorPaymentTermDTO, company: Company): VendorPaymentTermEntity {
      logger.trace("Validating Save VendorPaymentTerm {}", vo)

      doValidation { errors -> doSharedValidation(errors, vo, company) }

      return VendorPaymentTermEntity(vo = vo, company = company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, vo: VendorPaymentTermDTO, company: Company): VendorPaymentTermEntity {
      logger.trace("Validating Update VendorPaymentTerm {}", vo)

      val existing = vendorPaymentTermRepository.findOne(id, company) ?: throw NotFoundException(id)

      doValidation { errors -> doSharedValidation(errors, vo, company) }

      return VendorPaymentTermEntity(source = existing, updateWith = vo)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, vo: VendorPaymentTermDTO, company: Company) {
      if ((vo.discountDays != null || vo.discountMonth != null) && vo.discountPercent == null) {
         errors.add(ValidationError("discountPercent", NotNull("discountPercent")))
      } else if (vo.discountDays == null && vo.discountMonth == null && vo.discountPercent != null) {
         errors.add(ValidationError("discountPercent", NotUpdatable(vo.discountPercent)))
      } else if ((vo.discountDays != null || vo.discountMonth != null) && (vo.discountPercent!! > ONE)) {
            errors.add(ValidationError("discountPercent", MustBeInRangeOf("(0, 1]")))
      }

      val entity = VendorPaymentTermEntity(vo = vo, company = company)
      val percentageSum: BigDecimal = entity.scheduleRecords.asSequence().map { it.duePercent }.sum()

      if (!percentageSum.equalTo(ONE)) {
         val responseValue = percentageSum * BigDecimal(100)

         errors.add(ValidationError("scheduleRecords.duePercent[*]", VendorPaymentTermDuePercentDoesNotAddUp(responseValue.toFixed(5, 5))))
      }
   }
}
