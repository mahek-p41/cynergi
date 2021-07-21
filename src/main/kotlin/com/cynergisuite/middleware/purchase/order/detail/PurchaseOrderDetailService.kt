package com.cynergisuite.middleware.purchase.order.detail

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.purchase.order.detail.infrastructure.PurchaseOrderDetailRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderDetailService @Inject constructor(
   private val purchaseOrderDetailRepository: PurchaseOrderDetailRepository,
   private val purchaseOrderDetailValidator: PurchaseOrderDetailValidator
) {
   fun fetchById(id: UUID, company: Company): PurchaseOrderDetailDTO? =
      purchaseOrderDetailRepository.findOne(id, company)?.let { transformEntity(it) }

   fun create(dto: PurchaseOrderDetailDTO, company: Company): PurchaseOrderDetailDTO {
      val toCreate = purchaseOrderDetailValidator.validateCreate(dto, company)

      return transformEntity(purchaseOrderDetailRepository.insert(toCreate, company))
   }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<PurchaseOrderDetailDTO> {
      val found = purchaseOrderDetailRepository.findAll(company, pageRequest)

      return found.toPage { purchaseOrderDetailEntity: PurchaseOrderDetailEntity ->
         transformEntity(purchaseOrderDetailEntity)
      }
   }

   fun update(id: UUID, dto: PurchaseOrderDetailDTO, company: Company): PurchaseOrderDetailDTO {
      val toUpdate = purchaseOrderDetailValidator.validateUpdate(id, dto, company)

      return transformEntity(purchaseOrderDetailRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: Company) {
      purchaseOrderDetailRepository.delete(id, company)
   }

   private fun transformEntity(purchaseOrderDetailEntity: PurchaseOrderDetailEntity): PurchaseOrderDetailDTO {
      return PurchaseOrderDetailDTO(entity = purchaseOrderDetailEntity)
   }
}
