package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorPaymentTermService @Inject constructor(
   private val vendorPaymentTermRepository: VendorPaymentTermRepository,
   private val vendorPaymentTermValidator: VendorPaymentTermValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): VendorPaymentTermDTO? =
      vendorPaymentTermRepository.findOne(id, company)?.let { VendorPaymentTermDTO(entity = it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<VendorPaymentTermDTO> {
      val found = vendorPaymentTermRepository.findAll(company, pageRequest)

      return found.toPage { vendorPaymentTerm: VendorPaymentTermEntity ->
         VendorPaymentTermDTO(vendorPaymentTerm)
      }
   }

   fun create(dto: VendorPaymentTermDTO, company: CompanyEntity): VendorPaymentTermDTO {
      val toCreate = vendorPaymentTermValidator.validateCreate(dto, company)

      return VendorPaymentTermDTO(
         entity = vendorPaymentTermRepository.insert(entity = toCreate)
      )
   }

   fun update(id: UUID, dto: VendorPaymentTermDTO, company: CompanyEntity): VendorPaymentTermDTO {
      val toUpdate = vendorPaymentTermValidator.validateUpdate(id, dto, company)

      return VendorPaymentTermDTO(
         entity = vendorPaymentTermRepository.update(entity = toUpdate)
      )
   }
}
