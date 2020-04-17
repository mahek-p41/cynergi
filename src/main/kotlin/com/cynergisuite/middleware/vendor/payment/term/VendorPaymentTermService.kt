package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.ValidatorBase.Companion.logger
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class VendorPaymentTermService @Inject constructor(
   private val vendorPaymentTermRepository: VendorPaymentTermRepository,
   private val vendorPaymentTermValidator: VendorPaymentTermValidator
) {

   //TODO Why does AuditExceptionService.fetchById use "transformEntity" in its let?
   fun fetchById(id: Long, company: Company): VendorPaymentTermValueObject? =
      vendorPaymentTermRepository.findOne(id, company)?.let{ VendorPaymentTermValueObject(entity = it) }

   @Validated
   fun create(@Valid vo: VendorPaymentTermValueObject, company: Company): VendorPaymentTermValueObject {
      logger.debug("VPTS Create Before Validation VendorPaymentTermVO {}", vo)
      val toCreate = vendorPaymentTermValidator.validateCreate(vo, company)
      logger.debug("VPTS Create After Validation VendorPaymentTermEntity {}", toCreate)
      return VendorPaymentTermValueObject(
         entity = vendorPaymentTermRepository.insert(entity = toCreate)
      )
   }

   @Validated
   fun update(id: Long, @Valid vo: VendorPaymentTermValueObject, company: Company): VendorPaymentTermValueObject {
      val toUpdate = vendorPaymentTermValidator.validateUpdate(id, vo, company)

      return VendorPaymentTermValueObject(
         entity = vendorPaymentTermRepository.update(entity = toUpdate)
      )
   }


}
