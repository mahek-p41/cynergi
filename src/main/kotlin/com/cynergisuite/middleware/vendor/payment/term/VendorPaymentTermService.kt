package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
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

   fun fetchById(id: Long, company: Company): VendorPaymentTermValueObject? =
      vendorPaymentTermRepository.findOne(id)?.let{ VendorPaymentTermValueObject(entity = it) }

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest, company: Company): Page<VendorPaymentTermValueObject> {
      val found = vendorPaymentTermRepository.findAll(pageRequest, company)

      return found.toPage { vendorPaymentTerm: VendorPaymentTermEntity ->
         VendorPaymentTermValueObject(vendorPaymentTerm)
      }
   }

   fun exists(id: Long): Boolean =
      vendorPaymentTermRepository.exists(id = id)

   @Validated
   fun create(@Valid vo: VendorPaymentTermValueObject, company: Company): VendorPaymentTermValueObject {
      vendorPaymentTermValidator.validateCreate(vo)

      return VendorPaymentTermValueObject(
         entity = vendorPaymentTermRepository.insert(entity = VendorPaymentTermEntity(vo, company))
      )
   }

   @Validated
   fun update(@Valid vo: VendorPaymentTermValueObject, company: Company, id: Long): VendorPaymentTermValueObject {
      vendorPaymentTermValidator.validateUpdate(vo, id)
      //Will the validateUpdate
      return VendorPaymentTermValueObject(
         entity = vendorPaymentTermRepository.update(entity = VendorPaymentTermEntity(vo, company))
      )
   }
}
