package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderStatusTypeValueObject
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTypeValueObject
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostTypeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "PurchaseOrderControl", title = "Purchase order control", description = "Purchase order control entity")
data class PurchaseOrderControlDTO(

   @field:Positive
   @field:Schema(description = "Purchase order control id")
   var id: Long? = null,

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
   @field:Schema(description = "Default status type")
   var defaultStatusType: PurchaseOrderStatusTypeValueObject? = null,

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
   var defaultVendor: SimpleIdentifiableDTO?,

   @field:NotNull
   @field:Valid
   @field:Schema(description = "Update purchase order cost")
   var updatePurchaseOrderCost: UpdatePurchaseOrderCostTypeValueObject? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(description = "Default purchase order type")
   var defaultPurchaseOrderType: PurchaseOrderTypeValueObject? = null,

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
   var defaultApprover: SimpleIdentifiableDTO?,

   @field:NotNull
   @field:Schema(description = "Approval required flag type")
   var approvalRequiredFlagType: ApprovalRequiredFlagDTO? = null

) : Identifiable {

   constructor(
      entity: PurchaseOrderControlEntity,
      defaultStatusType: PurchaseOrderStatusTypeValueObject,
      updatePurchaseOrderCost: UpdatePurchaseOrderCostTypeValueObject,
      defaultPurchaseOrderType: PurchaseOrderTypeValueObject,
      approvalRequiredFlagType: ApprovalRequiredFlagDTO
   ) :
      this(
         id = entity.id,
         dropFiveCharactersOnModelNumber = entity.dropFiveCharactersOnModelNumber,
         updateAccountPayable = entity.updateAccountPayable,
         printSecondDescription = entity.printSecondDescription,
         defaultStatusType = defaultStatusType,
         printVendorComments = entity.printVendorComments,
         includeFreightInCost = entity.includeFreightInCost,
         updateCostOnModel = entity.updateCostOnModel,
         defaultVendor = entity.defaultVendor?.let { SimpleIdentifiableDTO(it) },
         updatePurchaseOrderCost = updatePurchaseOrderCost,
         defaultPurchaseOrderType = defaultPurchaseOrderType,
         sortByShipToOnPrint = entity.sortByShipToOnPrint,
         invoiceByLocation = entity.invoiceByLocation,
         validateInventory = entity.validateInventory,
         defaultApprover = entity.defaultApprover?.let { SimpleIdentifiableDTO(it) },
         approvalRequiredFlagType = approvalRequiredFlagType
      )

   override fun myId(): Long? = id
}
