package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostTypeValueObject
import com.cynergisuite.middleware.purchase.order.control.infrastructure.PurchaseOrderControlRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class PurchaseOrderControlService @Inject constructor(
   private val purchaseOrderControlValidator: PurchaseOrderControlValidator,
   private val purchaseOrderControlRepository: PurchaseOrderControlRepository
) {
   fun fetchOne(company: Company): PurchaseOrderControlDTO? {
      return purchaseOrderControlRepository.findOne(company)?.let { transformEntity(it) }
   }

   @Validated
   fun create(@Valid dto: PurchaseOrderControlDTO, company: Company): PurchaseOrderControlDTO {
      val toCreate = purchaseOrderControlValidator.validateCreate(dto, company)

      return transformEntity(purchaseOrderControlRepository.insert(toCreate, company))
   }

   @Validated
   fun update(id: Long, @Valid dto: PurchaseOrderControlDTO, company: Company): PurchaseOrderControlDTO {
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
