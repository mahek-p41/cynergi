package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusType
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.purchase.order.control.PurchaseOrderControlDTO
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagType
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderType
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorType
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusType
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderType
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeValueObject
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostType
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardType
import com.cynergisuite.middleware.shipping.freight.term.FreightTermType
import com.cynergisuite.middleware.shipping.location.ShipLocationType
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class PurchaseOrderEntity(
   val id: Long?,
   val number: Long,
   val vendor: VendorEntity,
   val statusType: PurchaseOrderStatusType,
   val orderDate: OffsetDateTime,
   val type: PurchaseOrderType,
   val freightOnboardType: FreightOnboardType,
   val freightTermType: FreightTermType,
   val shipLocationType: ShipLocationType,
   val approvedBy: EmployeeEntity,
   val totalAmount: BigDecimal,
   val receivedAmount: BigDecimal,
   val paidAmount: BigDecimal,
   val purchaseAgent: ?,
   val shipVia: ShipViaEntity,
   val requiredDate: LocalDate,
   val shipTo: VendorEntity?,
   val paymentTermType: VendorPaymentTermEntity,
   val message: String,
   val totalLandedAmount: BigDecimal,
   val totalFreightAmount: BigDecimal,
   val exceptionIndicatorType: ExceptionIndicatorType,
   val vendorSubmittedTime: LocalDateTime,
   val vendorSubmittedEmployee: EmployeeEntity,
   val ecommerceIndicator: Boolean,
   val customerAccount: AccountEntity

) : Identifiable {

   constructor(
      dto: PurchaseOrderControlDTO,
      defaultAccountPayableStatusType: DefaultAccountPayableStatusType,
      defaultVendor: VendorEntity?,
      updatePurchaseOrderCost: UpdatePurchaseOrderCostType,
      defaultPurchaseOrderType: DefaultPurchaseOrderType,
      defaultApprover: EmployeeEntity?,
      approvalRequiredFlagType: ApprovalRequiredFlagType
   ) :
      this(
         id = dto.id,
         number = dto.dropFiveCharactersOnModelNumber!!,
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

   override fun myId(): Long? = id
}
