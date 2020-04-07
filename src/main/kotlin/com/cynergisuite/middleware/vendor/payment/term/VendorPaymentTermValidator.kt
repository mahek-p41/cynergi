package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.localization.VendorPaymentTermDoesNotMatchDue
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.OffsetDateTime
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
      //The numberOfPayments determines up through which dueMonth# or dueDays# should be populated.
      //If numberOfPayments = 3, then one or both dueMonth and dueDay should be populated for 1-3.
      //duePercent must always be populated through the same value as numberofPayments.
      var pmts = vo.numberOfPayments!!
      while (pmts > 0) {
         when (pmts) {
            //Is there a way to have an error message be "either this field OR that field need attention?
            1 -> if (vo.dueMonth1 == null && vo.dueDays1 == null) errors.add(ValidationError("dueMonth1", VendorPaymentTermDoesNotMatchDue(1)))
            2 -> if (vo.dueMonth2 == null && vo.dueDays2 == null) errors.add(ValidationError("dueMonth2", VendorPaymentTermDoesNotMatchDue(2)))
            3 -> if (vo.dueMonth3 == null && vo.dueDays3 == null) errors.add(ValidationError("dueMonth3", VendorPaymentTermDoesNotMatchDue(3)))
            4 -> if (vo.dueMonth4 == null && vo.dueDays4 == null) errors.add(ValidationError("dueMonth4", VendorPaymentTermDoesNotMatchDue(4)))
            5 -> if (vo.dueMonth5 == null && vo.dueDays5 == null) errors.add(ValidationError("dueMonth5", VendorPaymentTermDoesNotMatchDue(5)))
            6 -> if (vo.dueMonth6 == null && vo.dueDays6 == null) errors.add(ValidationError("dueMonth6", VendorPaymentTermDoesNotMatchDue(6)))
         }
         when (pmts) {
            1 -> if (vo.duePercent1 == null) errors.add(ValidationError("duePercent1", VendorPaymentTermDoesNotMatchDue(1)))
            2 -> if (vo.duePercent2 == null) errors.add(ValidationError("duePercent2", VendorPaymentTermDoesNotMatchDue(2)))
            3 -> if (vo.duePercent3 == null) errors.add(ValidationError("duePercent3", VendorPaymentTermDoesNotMatchDue(3)))
            4 -> if (vo.duePercent4 == null) errors.add(ValidationError("duePercent4", VendorPaymentTermDoesNotMatchDue(4)))
            5 -> if (vo.duePercent5 == null) errors.add(ValidationError("duePercent5", VendorPaymentTermDoesNotMatchDue(5)))
            6 -> if (vo.duePercent6 == null) errors.add(ValidationError("duePercent6", VendorPaymentTermDoesNotMatchDue(6)))
         }
         pmts--
      }

      if((vo.discountDays != null || vo.discountMonth != null) && vo.discountPercent == null) {
         errors.add(ValidationError("discountPercent", NotNull("discountPercent")))
      }

      var percentSum: BigDecimal
      val oneHundred: BigDecimal
      val whole: Int
      whole = 100
      oneHundred = whole.toBigDecimal()
      percentSum = vo.duePercent1!!
      if (vo.duePercent2 != null) percentSum = percentSum.add(vo.duePercent2)
      if (vo.duePercent3 != null) percentSum = percentSum.add(vo.duePercent3)
      if (vo.duePercent4 != null) percentSum = percentSum.add(vo.duePercent4)
      if (vo.duePercent5 != null) percentSum = percentSum.add(vo.duePercent5)
      if (vo.duePercent6 != null) percentSum = percentSum.add(vo.duePercent6)
      if(percentSum.compareTo(oneHundred) != 0) errors.add(ValidationError("duePercent1", VendorPaymentTermDoesNotMatchDue(1)))

      //if(vo.dueMonth1 == null) {
      //   errors.add(ValidationError("dueMonth1", NotNull("dueMonth1")))
      //}
      //if(vo.dueDays1 == null) {
      //   errors.add(ValidationError("dueDays1", NotNull("dueDays1")))
      //}
   }
}
