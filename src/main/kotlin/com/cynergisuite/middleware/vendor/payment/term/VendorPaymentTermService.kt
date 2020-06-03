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

   fun fetchById(id: Long, company: Company): VendorPaymentTermDTO? =
      vendorPaymentTermRepository.findOne(id, company)?.let{ VendorPaymentTermDTO(entity = it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<VendorPaymentTermDTO> {
      val found = vendorPaymentTermRepository.findAll(company, pageRequest)

      return found.toPage { vendorPaymentTerm: VendorPaymentTermEntity ->
         VendorPaymentTermDTO(vendorPaymentTerm)
      }
   }

   @Validated
   fun create(@Valid vo: VendorPaymentTermDTO, company: Company): VendorPaymentTermDTO {
      val toCreate = vendorPaymentTermValidator.validateCreate(vo, company)

      return VendorPaymentTermDTO(
         entity = vendorPaymentTermRepository.insert(entity = toCreate)
      )
   }

   @Validated
   fun update(id: Long, @Valid vo: VendorPaymentTermDTO, company: Company): VendorPaymentTermDTO {
      val toUpdate = vendorPaymentTermValidator.validateUpdate(id, vo, company)

      return VendorPaymentTermDTO(
         entity = vendorPaymentTermRepository.update(entity = toUpdate)
      )
   }
}
