package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagType
import com.cynergisuite.middleware.purchase.order.PurchaseOrderStatusType
import com.cynergisuite.middleware.purchase.order.PurchaseOrderType
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostType
import com.cynergisuite.middleware.vendor.VendorEntity
import java.time.OffsetDateTime

data class PurchaseOrderControlEntity(
   val id: Long?,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val dropFiveCharactersOnModelNumber: Boolean,
   val updateAccountPayable: Boolean,
   val printSecondDescription: Boolean,
   val defaultStatusType: PurchaseOrderStatusType,
   val printVendorComments: Boolean,
   val includeFreightInCost: Boolean,
   val updateCostOnModel: Boolean,
   val defaultVendor: VendorEntity? = null,
   val updatePurchaseOrderCost: UpdatePurchaseOrderCostType,
   val defaultPurchaseOrderType: PurchaseOrderType,
   val sortByShipToOnPrint: Boolean,
   val invoiceByLocation: Boolean,
   val validateInventory: Boolean,
   val defaultApprover: EmployeeEntity? = null,
   val approvalRequiredFlagType: ApprovalRequiredFlagType
) : Identifiable {

   constructor(
      dto: PurchaseOrderControlDTO,
      defaultStatusType: PurchaseOrderStatusType,
      defaultVendor: VendorEntity?,
      updatePurchaseOrderCost: UpdatePurchaseOrderCostType,
      defaultPurchaseOrderType: PurchaseOrderType,
      defaultApprover: EmployeeEntity?,
      approvalRequiredFlagType: ApprovalRequiredFlagType
   ) :
      this(
         id = dto.id,
         dropFiveCharactersOnModelNumber = dto.dropFiveCharactersOnModelNumber!!,
         updateAccountPayable = dto.updateAccountPayable!!,
         printSecondDescription = dto.printSecondDescription!!,
         defaultStatusType = defaultStatusType,
         printVendorComments = dto.printVendorComments!!,
         includeFreightInCost = dto.includeFreightInCost!!,
         updateCostOnModel = dto.updateCostOnModel!!,
         defaultVendor = defaultVendor,
         updatePurchaseOrderCost = updatePurchaseOrderCost,
         defaultPurchaseOrderType = defaultPurchaseOrderType,
         sortByShipToOnPrint = dto.sortByShipToOnPrint!!,
         invoiceByLocation = dto.invoiceByLocation!!,
         validateInventory = dto.validateInventory!!,
         defaultApprover = defaultApprover,
         approvalRequiredFlagType = approvalRequiredFlagType
      )

   override fun myId(): Long? = id
}
