package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AuditOpenAtStore
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
   fun validateCreate(vo: VendorPaymentTermValueObject): VendorPaymentTermEntity {
      logger.trace("Validating Create VendorPaymentTerm {}", vo)

      //The numberOfPayments determines up through which dueMonth# or dueDays# should be populated.
      //If numberOfPayments = 3, then one or both dueMonth and dueDay should be populated for 1-3.
      //duePercent must always be populated through the same value as numberofPayments.
      when (vo.numberOfPayments) {
         1 -> if (vo.dueMonth1 == null && vo.dueDays1 == null ) {errors.add(ValidationError("We have 2 possibilities here, so what do?", NotFound(DueDay1 or DueMonth1)))}
      }

      if((vo.discountDays != null || vo.discountMonth != null) && vo.discountPercent == null) {
         errors.add(ValidationError("discountPercent", NotFound(discountPercent)))
      }

      //If errors are encountered above, does it stop things somehow?
      //Why does the validation return anything as opposed to just stopping the user with an alert if they dp something wrong?

      //The below is from AuditValidator. Why is it forcing in hardcoded values?
      return VendorPaymentTermEntity(
         store = storeRepository.findOne(number = audit.store!!.number!!, company = user.myCompany())!!,
         number = 0,
         totalDetails = 0,
         totalExceptions = 0,
         hasExceptionNotes = false,
         lastUpdated = OffsetDateTime.now(),
         inventoryCount = 0,
         actions = mutableSetOf(
            AuditActionEntity(
               status = CREATED,
               changedBy = employeeRepository.findOne(user)!!
            )
         )
      )
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: VendorPaymentTermValueObject, idIn: Long){
      logger.trace("Validating Update VendorPaymentTerm {}", vo)
      //Why does this function need the id? We are checking if the id is null, if we have one to send,
      //we know it's not null.
      doValidation { errors ->
         val id = idIn

         if (id == null) {
            errors.add(element = ValidationError("id", NotNull("id")))
         } else if ( !vendorPaymentTermRepository.exists(id = id) ) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }
   }
}
