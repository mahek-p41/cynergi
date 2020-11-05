package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostTypeValueObject
import com.cynergisuite.middleware.purchase.order.control.infrastructure.PurchaseOrderControlRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderControlService @Inject constructor(
   private val purchaseOrderControlValidator: PurchaseOrderControlValidator,
   private val purchaseOrderControlRepository: PurchaseOrderControlRepository
) {
   fun fetchOne(company: Company): PurchaseOrderControlDTO? {
      return purchaseOrderControlRepository.findOne(company)?.let { transformEntity(it) }
   }

   fun create(dto: PurchaseOrderControlDTO, company: Company): PurchaseOrderControlDTO {
      val toCreate = purchaseOrderControlValidator.validateCreate(dto, company)

      return transformEntity(purchaseOrderControlRepository.insert(toCreate, company))
   }

   fun update(id: Long, dto: PurchaseOrderControlDTO, company: Company): PurchaseOrderControlDTO {
      val toUpdate = purchaseOrderControlValidator.validateUpdate(id, dto, company)

      return transformEntity(purchaseOrderControlRepository.update(toUpdate, company))
   }

   private fun transformEntity(purchaseOrderControl: PurchaseOrderControlEntity): PurchaseOrderControlDTO {
      return PurchaseOrderControlDTO(
         entity = purchaseOrderControl,
         defaultAccountPayableStatusType = DefaultAccountPayableStatusTypeDTO(purchaseOrderControl.defaultAccountPayableStatusType),
         updatePurchaseOrderCost = UpdatePurchaseOrderCostTypeValueObject(purchaseOrderControl.updatePurchaseOrderCost),
         defaultPurchaseOrderType = DefaultPurchaseOrderTypeDTO(purchaseOrderControl.defaultPurchaseOrderType),
         approvalRequiredFlagType = ApprovalRequiredFlagDTO(purchaseOrderControl.approvalRequiredFlagType)
      )
   }
}
