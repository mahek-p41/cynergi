package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class PurchaseOrderService @Inject constructor(
   private val purchaseOrderRepository: PurchaseOrderRepository,
   private val purchaseOrderValidator: PurchaseOrderValidator
) {
   fun fetchById(id: UUID, company: CompanyEntity): PurchaseOrderDTO? =
      purchaseOrderRepository.findOne(id, company)?.let { transformEntity(it) }

   fun create(dto: PurchaseOrderDTO, company: CompanyEntity): PurchaseOrderDTO {
      val toCreate = purchaseOrderValidator.validateCreate(dto, company)

      return transformEntity(purchaseOrderRepository.insert(toCreate, company))
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<PurchaseOrderDTO> {
      val found = purchaseOrderRepository.findAll(company, pageRequest)

      return found.toPage { purchaseOrderEntity: PurchaseOrderEntity ->
         transformEntity(purchaseOrderEntity)
      }
   }

   fun update(id: UUID, dto: PurchaseOrderDTO, company: CompanyEntity): PurchaseOrderDTO {
      val toUpdate = purchaseOrderValidator.validateUpdate(id, dto, company)

      return transformEntity(purchaseOrderRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      purchaseOrderRepository.delete(id, company)
   }

   private fun transformEntity(purchaseOrderEntity: PurchaseOrderEntity): PurchaseOrderDTO {
      return PurchaseOrderDTO(entity = purchaseOrderEntity)
   }
}
