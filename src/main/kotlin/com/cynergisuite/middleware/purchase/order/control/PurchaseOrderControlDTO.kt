package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeValueObject
import com.cynergisuite.middleware.vendor.VendorDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "PurchaseOrderControl", title = "Purchase order control", description = "Purchase order control entity")
data class PurchaseOrderControlDTO(

   @field:Schema(description = "Purchase order control id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Drop five characters on model number")
   var dropFiveCharactersOnModelNumber: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Update account payable")
   var updateAccountPayable: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Print second description")
   var printSecondDescription: Boolean? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(description = "Default account payable status type")
   var defaultAccountPayableStatusType: DefaultAccountPayableStatusTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Print vendor comments")
   var printVendorComments: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Include freight in cost")
   var includeFreightInCost: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Update cost on model")
   var updateCostOnModel: Boolean? = null,

   @field:Schema(description = "Default vendor")
   var defaultVendor: VendorDTO? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(description = "Update purchase order cost")
   var updatePurchaseOrderCost: UpdatePurchaseOrderCostTypeValueObject? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(description = "Default purchase order type")
   var defaultPurchaseOrderType: DefaultPurchaseOrderTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Sort by ship to on print")
   var sortByShipToOnPrint: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Invoice by location")
   var invoiceByLocation: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Validate inventory")
   var validateInventory: Boolean? = null,

   @field:Schema(description = "Default approver")
   var defaultApprover: EmployeeValueObject? = null,

   @field:NotNull
   @field:Schema(description = "Approval required flag type")
   var approvalRequiredFlagType: ApprovalRequiredFlagDTO? = null

) : Identifiable {

   constructor(
      entity: PurchaseOrderControlEntity,
      defaultAccountPayableStatusType: DefaultAccountPayableStatusTypeDTO,
      defaultVendor: VendorDTO?,
      updatePurchaseOrderCost: UpdatePurchaseOrderCostTypeValueObject,
      defaultPurchaseOrderType: DefaultPurchaseOrderTypeDTO,
      approvalRequiredFlagType: ApprovalRequiredFlagDTO
   ) :
      this(
         id = entity.id,
         dropFiveCharactersOnModelNumber = entity.dropFiveCharactersOnModelNumber,
         updateAccountPayable = entity.updateAccountPayable,
         printSecondDescription = entity.printSecondDescription,
         defaultAccountPayableStatusType = defaultAccountPayableStatusType,
         printVendorComments = entity.printVendorComments,
         includeFreightInCost = entity.includeFreightInCost,
         updateCostOnModel = entity.updateCostOnModel,
         defaultVendor = defaultVendor,
         updatePurchaseOrderCost = updatePurchaseOrderCost,
         defaultPurchaseOrderType = defaultPurchaseOrderType,
         sortByShipToOnPrint = entity.sortByShipToOnPrint,
         invoiceByLocation = entity.invoiceByLocation,
         validateInventory = entity.validateInventory,
         defaultApprover = entity.defaultApprover?.let { EmployeeValueObject(it) },
         approvalRequiredFlagType = approvalRequiredFlagType
      )

   override fun myId(): UUID? = id
}
