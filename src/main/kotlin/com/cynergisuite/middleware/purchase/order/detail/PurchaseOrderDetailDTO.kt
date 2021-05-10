package com.cynergisuite.middleware.purchase.order.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderRequisitionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "PurchaseOrderDetail", title = "An entity containing purchase order detail information", description = "An entity containing purchase order detail information.")
data class PurchaseOrderDetailDTO(

   @field:Positive
   @field:Schema(name = "id", description = "Purchase order detail id")
   var id: Long? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "number", description = "Purchase order detail number")
   var number: Long? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "purchaseOrder", description = "Purchase order")
   var purchaseOrder: PurchaseOrderDTO? = null,

   @field:Positive
   @field:Schema(name = "sequence", description = "Sequence")
   var sequence: Int? = null,

   @field:NotNull
   @field:Schema(name = "itemfileNumber", description = "Itemfile number")
   var itemfileNumber: String? = null,

   @field:NotNull
   @field:Schema(name = "orderQuantity", description = "Order quantity")
   var orderQuantity: Int? = null,

   @field:NotNull
   @field:Schema(name = "receivedQuantity", description = "Received quantity")
   var receivedQuantity: Int? = null,

   @field:NotNull
   @field:Positive
   @field:Digits(integer = 11, fraction = 3)
   @field:Schema(name = "cost", description = "Cost")
   var cost: BigDecimal? = null,

   @field:Schema(name = "message", description = "Message", required = false)
   var message: String? = null,

   @field:Schema(name = "color", description = "Color id", required = false)
   var color: Int? = null,

   @field:Schema(name = "fabric", description = "Fabric id", required = false)
   var fabric: Int? = null,

   @field:Schema(name = "cancelledQuantity", description = "Cancelled quantity", required = false)
   var cancelledQuantity: Int? = null,

   @field:Schema(name = "cancelledTempQuantity", description = "Cancelled temp quantity", required = false)
   var cancelledTempQuantity: Int? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "shipTo", description = "Ship to id")
   var shipTo: SimpleIdentifiableDTO? = null,

   @field:Schema(name = "requiredDate", description = "Required date", required = false)
   var requiredDate: LocalDate? = null,

   @field:Schema(name = "dateOrdered", description = "Date ordered", required = false)
   var dateOrdered: LocalDate? = null,

   @field:Positive
   @field:Digits(integer = 11, fraction = 2)
   @field:Schema(name = "freightPerItem", description = "Freight per item", required = false)
   var freightPerItem: BigDecimal? = null,

   @field:Schema(name = "tempQuantityToReceive", description = "Temp quantity to receive", required = false)
   var tempQuantityToReceive: Int? = null,

   @field:NotNull
   @field:Schema(name = "vendor", description = "Vendor id")
   var vendor: SimpleIdentifiableDTO? = null,

   @field:Schema(name = "lastReceivedDate", description = "Last received date", required = false)
   var lastReceivedDate: LocalDate? = null,

   @field:Positive
   @field:Digits(integer = 11, fraction = 3)
   @field:Schema(name = "landedCost", description = "Landed cost", required = false)
   var landedCost: BigDecimal? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "statusType", description = "Purchase order status type")
   var statusType: PurchaseOrderStatusTypeValueObject? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "purchaseOrderRequisitionIndicatorType", description = "Purchase order requisition indicator type")
   var purchaseOrderRequisitionIndicatorType: PurchaseOrderRequisitionIndicatorTypeDTO? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "exceptionIndicatorType", description = "Exception indicator type")
   var exceptionIndicatorType: ExceptionIndicatorTypeDTO? = null,

   @field:NotNull
   @field:Schema(name = "convertedPurchaseOrderNumber", description = "Converted purchase order number")
   var convertedPurchaseOrderNumber: Int? = null,

   @field:NotNull
   @field:Schema(name = "approvedIndicator", description = "Approved indicator")
   var approvedIndicator: Boolean? = null

) : Identifiable {

   constructor(entity: PurchaseOrderDetailEntity) :
      this(
         id = entity.id,
         number = entity.number,
         purchaseOrder = PurchaseOrderDTO(entity.purchaseOrder),
         sequence = entity.sequence,
         itemfileNumber = entity.itemfileNumber,
         orderQuantity = entity.orderQuantity,
         receivedQuantity = entity.receivedQuantity,
         cost = entity.cost,
         message = entity.message,
         color = entity.color,
         fabric = entity.fabric,
         cancelledQuantity = entity.cancelledQuantity,
         cancelledTempQuantity = entity.cancelledTempQuantity,
         shipTo = SimpleIdentifiableDTO(entity.shipTo),
         requiredDate = entity.requiredDate,
         dateOrdered = entity.dateOrdered,
         freightPerItem = entity.freightPerItem,
         tempQuantityToReceive = entity.tempQuantityToReceive,
         vendor = SimpleIdentifiableDTO(entity.vendor),
         lastReceivedDate = entity.lastReceivedDate,
         landedCost = entity.landedCost,
         statusType = PurchaseOrderStatusTypeValueObject(entity.statusType),
         purchaseOrderRequisitionIndicatorType = PurchaseOrderRequisitionIndicatorTypeDTO(entity.purchaseOrderRequisitionIndicatorType),
         exceptionIndicatorType = ExceptionIndicatorTypeDTO(entity.exceptionIndicatorType),
         convertedPurchaseOrderNumber = entity.convertedPurchaseOrderNumber,
         approvedIndicator = entity.approvedIndicator
      )

   override fun myId(): Long? = id
}
