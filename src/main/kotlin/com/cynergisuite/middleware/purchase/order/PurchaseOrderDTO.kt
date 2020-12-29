package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeValueObject
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeValueObject
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeDTO
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "PurchaseOrder", title = "An entity containing purchase order information", description = "An entity containing purchase order information.")
data class PurchaseOrderDTO(

   @field:Positive
   @field:Schema(name = "id", description = "Purchase order id")
   var id: Long? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "number", description = "Purchase order number")
   var number: Long? = null,

   @field:NotNull
   @field:Schema(name = "vendor", description = "Vendor id")
   var vendor: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "statusType", description = "Purchase order status type")
   var statusType: PurchaseOrderStatusTypeValueObject? = null,

   @field:NotNull
   @field:Schema(name = "orderDate", description = "Order date")
   var orderDate: LocalDate? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "type", description = "Purchase order type")
   var type: PurchaseOrderTypeValueObject? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "freightOnboardType", description = "Freight onboard type")
   var freightOnboardType: FreightOnboardTypeDTO? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "freightTermType", description = "Freight term type")
   var freightTermType: FreightTermTypeDTO? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "shipLocationType", description = "Ship location type")
   var shipLocationType: ShipLocationTypeDTO? = null,

   @field:NotNull
   @field:Schema(name = "approvedBy", description = "Approved by number")
   var approvedBy: EmployeeValueObject? = null,

   @field:Digits(integer = 11, fraction = 2)
   @field:Schema(name = "totalAmount", description = "Total amount", required = false)
   var totalAmount: BigDecimal? = null,

   @field:Digits(integer = 11, fraction = 2)
   @field:Schema(name = "receivedAmount", description = "Received amount", required = false)
   var receivedAmount: BigDecimal? = null,

   @field:Digits(integer = 11, fraction = 2)
   @field:Schema(name = "paidAmount", description = "Paid amount", required = false)
   var paidAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(name = "purchaseAgent", description = "Purchase agent number")
   var purchaseAgent: EmployeeValueObject? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "shipVia", description = "Ship via", implementation = SimpleIdentifiableDTO::class)
   var shipVia: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(name = "requiredDate", description = "Required date")
   var requiredDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "shipTo", description = "Ship to vendor id")
   var shipTo: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(name = "paymentTermType", description = "Vendor payment term")
   var paymentTermType: SimpleIdentifiableDTO? = null,

   @field:Schema(name = "message", description = "Purchase order message", required = false)
   var message: String? = null,

   @field:Digits(integer = 11, fraction = 2)
   @field:Schema(name = "totalLandedAmount", description = "Total landed amount", required = false)
   var totalLandedAmount: BigDecimal? = null,

   @field:Digits(integer = 11, fraction = 2)
   @field:Schema(name = "totalFreightAmount", description = "Total freight amount", required = false)
   var totalFreightAmount: BigDecimal? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "exceptionIndicatorType", description = "Exception indicator type")
   var exceptionIndicatorType: ExceptionIndicatorTypeDTO? = null,

   @field:Schema(name = "vendorSubmittedTime", description = "Vendor submitted time", required = false)
   var vendorSubmittedTime: OffsetDateTime?,

   @field:Schema(name = "vendorSubmittedEmployee", description = "Vendor submitted employee number", required = false)
   var vendorSubmittedEmployee: EmployeeValueObject? = null,

   @field:NotNull
   @field:Schema(name = "ecommerceIndicator", description = "Ecommerce indicator")
   var ecommerceIndicator: Boolean? = null,

   @field:Schema(name = "customerAccount", description = "Customer account number", required = false)
   var customerAccount: SimpleIdentifiableDTO?

) : Identifiable {

   constructor(entity: PurchaseOrderEntity) :
      this(
         id = entity.id,
         number = entity.number,
         vendor = SimpleIdentifiableDTO(entity.vendor),
         statusType = PurchaseOrderStatusTypeValueObject(entity.statusType),
         orderDate = entity.orderDate,
         type = PurchaseOrderTypeValueObject(entity.type),
         freightOnboardType = FreightOnboardTypeDTO(entity.freightOnboardType),
         freightTermType = FreightTermTypeDTO(entity.freightTermType),
         shipLocationType = ShipLocationTypeDTO(entity.shipLocationType),
         approvedBy = EmployeeValueObject(entity.approvedBy),
         totalAmount = entity.totalAmount,
         receivedAmount = entity.receivedAmount,
         paidAmount = entity.paidAmount,
         purchaseAgent = EmployeeValueObject(entity.purchaseAgent),
         shipVia = SimpleIdentifiableDTO(entity.shipVia),
         requiredDate = entity.requiredDate,
         shipTo = SimpleIdentifiableDTO(entity.shipTo),
         paymentTermType = SimpleIdentifiableDTO(entity.paymentTermType),
         message = entity.message,
         totalLandedAmount = entity.totalLandedAmount,
         totalFreightAmount = entity.totalFreightAmount,
         exceptionIndicatorType = ExceptionIndicatorTypeDTO(entity.exceptionIndicatorType),
         vendorSubmittedTime = entity.vendorSubmittedTime,
         vendorSubmittedEmployee = entity.vendorSubmittedEmployee?.let { EmployeeValueObject(it) },
         ecommerceIndicator = entity.ecommerceIndicator,
         customerAccount = entity.customerAccount?.let { SimpleIdentifiableDTO(it) }
      )

   override fun myId(): Long? = id
}
