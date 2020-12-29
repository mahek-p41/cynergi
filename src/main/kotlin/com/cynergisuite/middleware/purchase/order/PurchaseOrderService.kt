package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderService @Inject constructor(
   private val purchaseOrderRepository: PurchaseOrderRepository,
   private val purchaseOrderValidator: PurchaseOrderValidator
) {
   fun fetchById(id: Long, company: Company): PurchaseOrderDTO? =
      purchaseOrderRepository.findOne(id, company)?.let { transformEntity(it) }

   fun create(dto: PurchaseOrderDTO, company: Company): PurchaseOrderDTO {
      val toCreate = purchaseOrderValidator.validateCreate(dto, company)

      return transformEntity(purchaseOrderRepository.insert(toCreate, company))
   }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<PurchaseOrderDTO> {
      val found = purchaseOrderRepository.findAll(company, pageRequest)

      return found.toPage { purchaseOrderEntity: PurchaseOrderEntity ->
         transformEntity(purchaseOrderEntity)
      }
   }

   fun update(id: Long, dto: PurchaseOrderDTO, company: Company): PurchaseOrderDTO {
      val toUpdate = purchaseOrderValidator.validateUpdate(id, dto, company)

      return transformEntity(purchaseOrderRepository.update(toUpdate, company))
   }

   fun delete(id: Long, company: Company) {
      purchaseOrderRepository.delete(id, company)
   }

   private fun transformEntity(purchaseOrderEntity: PurchaseOrderEntity): PurchaseOrderDTO {
      return PurchaseOrderDTO(entity = purchaseOrderEntity)
   }
}
