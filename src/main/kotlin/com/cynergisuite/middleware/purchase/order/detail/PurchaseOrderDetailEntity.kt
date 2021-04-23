package com.cynergisuite.middleware.purchase.order.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.purchase.order.PurchaseOrderEntity
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorType
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderRequisitionIndicatorType
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusType
import com.cynergisuite.middleware.vendor.VendorEntity
import java.math.BigDecimal
import java.time.LocalDate

data class PurchaseOrderDetailEntity(
   val id: Long?,
   val number: Long,
   val purchaseOrder: PurchaseOrderEntity,
   val sequence: Int?,
   val itemfileNumber: String,
   val orderQuantity: Int,
   val receivedQuantity: Int,
   val cost: BigDecimal,
   val message: String?,
   val color: Int?,
   val fabric: Int?,
   val cancelledQuantity: Int?,
   val cancelledTempQuantity: Int?,
   val shipTo: Identifiable,
   val requiredDate: LocalDate?,
   val dateOrdered: LocalDate?,
   val freightPerItem: BigDecimal?,
   val tempQuantityToReceive: Int?,
   val vendor: VendorEntity,
   val lastReceivedDate: LocalDate?,
   val landedCost: BigDecimal?,
   val statusType: PurchaseOrderStatusType,
   val purchaseOrderRequisitionIndicatorType: PurchaseOrderRequisitionIndicatorType,
   val exceptionIndicatorType: ExceptionIndicatorType,
   val convertedPurchaseOrderNumber: Int,
   val approvedIndicator: Boolean

) : Identifiable {

   constructor(
      dto: PurchaseOrderDetailDTO,
      purchaseOrder: PurchaseOrderEntity,
      shipTo: SimpleIdentifiableEntity,
      vendor: VendorEntity,
      statusType: PurchaseOrderStatusType,
      purchaseOrderRequisitionIndicatorType: PurchaseOrderRequisitionIndicatorType,
      exceptionIndicatorType: ExceptionIndicatorType
   ) :
      this(
         id = dto.id,
         number = dto.number!!,
         purchaseOrder = purchaseOrder,
         sequence = dto.sequence,
         itemfileNumber = dto.itemfileNumber!!,
         orderQuantity = dto.orderQuantity!!,
         receivedQuantity = dto.receivedQuantity!!,
         cost = dto.cost!!,
         message = dto.message,
         color = dto.color,
         fabric = dto.fabric,
         cancelledQuantity = dto.cancelledQuantity,
         cancelledTempQuantity = dto.cancelledTempQuantity,
         shipTo = shipTo,
         requiredDate = dto.requiredDate,
         dateOrdered = dto.dateOrdered,
         freightPerItem = dto.freightPerItem,
         tempQuantityToReceive = dto.tempQuantityToReceive,
         vendor = vendor,
         lastReceivedDate = dto.lastReceivedDate,
         landedCost = dto.landedCost,
         statusType = statusType,
         purchaseOrderRequisitionIndicatorType = purchaseOrderRequisitionIndicatorType,
         exceptionIndicatorType = exceptionIndicatorType,
         convertedPurchaseOrderNumber = dto.convertedPurchaseOrderNumber!!,
         approvedIndicator = dto.approvedIndicator!!
      )

   override fun myId(): Long? = id
}
