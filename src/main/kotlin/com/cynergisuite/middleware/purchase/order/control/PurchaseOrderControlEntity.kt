package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusType
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagType
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderType
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostType
import com.cynergisuite.middleware.vendor.VendorEntity
import java.time.OffsetDateTime
import java.util.UUID

data class PurchaseOrderControlEntity(
   val id: UUID? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val dropFiveCharactersOnModelNumber: Boolean,
   val updateAccountPayable: Boolean,
   val printSecondDescription: Boolean,
   val defaultAccountPayableStatusType: DefaultAccountPayableStatusType,
   val printVendorComments: Boolean,
   val includeFreightInCost: Boolean,
   val updateCostOnModel: Boolean,
   val defaultVendor: VendorEntity? = null,
   val updatePurchaseOrderCost: UpdatePurchaseOrderCostType,
   val defaultPurchaseOrderType: DefaultPurchaseOrderType,
   val sortByShipToOnPrint: Boolean,
   val invoiceByLocation: Boolean,
   val validateInventory: Boolean,
   val defaultApprover: EmployeeEntity? = null,
   val approvalRequiredFlagType: ApprovalRequiredFlagType
) : Identifiable {

   constructor(
      id: UUID?,
      dto: PurchaseOrderControlDTO,
      defaultAccountPayableStatusType: DefaultAccountPayableStatusType,
      defaultVendor: VendorEntity?,
      updatePurchaseOrderCost: UpdatePurchaseOrderCostType,
      defaultPurchaseOrderType: DefaultPurchaseOrderType,
      defaultApprover: EmployeeEntity?,
      approvalRequiredFlagType: ApprovalRequiredFlagType
   ) :
      this(
         id = id,
         dropFiveCharactersOnModelNumber = dto.dropFiveCharactersOnModelNumber!!,
         updateAccountPayable = dto.updateAccountPayable!!,
         printSecondDescription = dto.printSecondDescription!!,
         defaultAccountPayableStatusType = defaultAccountPayableStatusType,
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

   override fun myId(): UUID? = id
}
