package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorType
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusType
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderType
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardType
import com.cynergisuite.middleware.shipping.freight.term.FreightTermType
import com.cynergisuite.middleware.shipping.location.ShipLocationType
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class PurchaseOrderEntity(
   val id: UUID?,
   val number: Long,
   val vendor: VendorEntity,
   val statusType: PurchaseOrderStatusType,
   val orderDate: LocalDate,
   val type: PurchaseOrderType,
   val freightOnboardType: FreightOnboardType,
   val freightTermType: FreightTermType,
   val shipLocationType: ShipLocationType,
   val approvedBy: EmployeeEntity,
   val totalAmount: BigDecimal?,
   val receivedAmount: BigDecimal?,
   val paidAmount: BigDecimal?,
   val purchaseAgent: EmployeeEntity,
   val shipVia: ShipViaEntity,
   val requiredDate: LocalDate,
   val shipTo: StoreEntity,
   val paymentTermType: VendorPaymentTermEntity,
   val message: String?,
   val totalLandedAmount: BigDecimal?,
   val totalFreightAmount: BigDecimal?,
   val exceptionIndicatorType: ExceptionIndicatorType,
   val vendorSubmittedTime: OffsetDateTime?,
   val vendorSubmittedEmployee: EmployeeEntity?,
   val ecommerceIndicator: Boolean,
) : Identifiable {

   constructor(
      existingEntity: PurchaseOrderEntity?,
      dto: PurchaseOrderDTO,
      vendor: VendorEntity,
      statusType: PurchaseOrderStatusType,
      type: PurchaseOrderType,
      freightOnboardType: FreightOnboardType,
      freightTermType: FreightTermType,
      shipLocationType: ShipLocationType,
      approvedBy: EmployeeEntity,
      purchaseAgent: EmployeeEntity,
      shipVia: ShipViaEntity,
      shipTo: StoreEntity,
      paymentTermType: VendorPaymentTermEntity,
      exceptionIndicatorType: ExceptionIndicatorType,
      vendorSubmittedEmployee: EmployeeEntity?,
   ) :
      this(
         id = existingEntity?.id,
         number = dto.number!!,
         vendor = vendor,
         statusType = statusType,
         orderDate = dto.orderDate!!,
         type = type,
         freightOnboardType = freightOnboardType,
         freightTermType = freightTermType,
         shipLocationType = shipLocationType,
         approvedBy = approvedBy,
         totalAmount = dto.totalAmount,
         receivedAmount = dto.receivedAmount,
         paidAmount = dto.paidAmount,
         purchaseAgent = purchaseAgent,
         shipVia = shipVia,
         requiredDate = dto.requiredDate!!,
         shipTo = shipTo,
         paymentTermType = paymentTermType,
         message = dto.message,
         totalLandedAmount = dto.totalLandedAmount,
         totalFreightAmount = dto.totalFreightAmount,
         exceptionIndicatorType = exceptionIndicatorType,
         vendorSubmittedTime = dto.vendorSubmittedTime,
         vendorSubmittedEmployee = vendorSubmittedEmployee,
         ecommerceIndicator = dto.ecommerceIndicator!!,
      )

   override fun myId(): UUID? = id
}
