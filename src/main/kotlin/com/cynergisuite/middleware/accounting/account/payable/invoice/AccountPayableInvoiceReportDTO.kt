package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableInvoiceReport", title = "Account Payable Invoice Report", description = "Account Payable Invoice Report")
data class AccountPayableInvoiceReportDTO(

   @field:Schema(description = "Account Payable Invoice ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Vendor name")
   var vendorName: String? = null,

   @field:NotNull
   @field:Schema(description = "Vendor group")
   var vendorGroup: String? = null,

   @field:NotNull
   @field:Schema(description = "Invoice")
   var invoice: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice type value")
   var type: String? = null,

   @field:NotNull
   @field:Schema(description = "PO header number")
   var poHeaderNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice date")
   var invoiceDate: LocalDate? = null,

   @field:Schema(description = "Account payable invoice date cleared", required = false)
   var entryDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice status id")
   var status: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable discount taken")
   var discountTaken: BigDecimal? = null,

   @field:Schema(description = "Account payable invoice due date", required = false)
   var dueDate: LocalDate? = null,

   @field:Schema(description = "Account payable invoice expense date", required = false)
   var expenseDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice amount")
   var paidAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Bank number")
   var bankNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Payment type value")
   var pmtType: String? = null,

   @field:NotNull
   @field:Schema(description = "Payment number")
   var pmtNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice message")
   var notes: String? = null,

   private var acctNumber: Int? = null,
   private var acctName: String? = null,
   private var distCenter: String? = null,
   private var distAmount: BigDecimal? = null,

   // 1 or multiple payment details
   @field:Schema(description = "Listing of Payment Details associated with this Invoice", required = false, accessMode = Schema.AccessMode.READ_ONLY)
   var invoiceDetails: MutableSet<AccountPayablePaymentDetailReportDTO> = mutableSetOf(),

   // 1 or multiple AccountPayableDistributionEntity
   var distDetails: MutableSet<AccountPayableDistDetailReportDTO> = mutableSetOf(),

   // 1 or multiple inventory units
   var inventories: MutableSet<AccountPayableInventoryReportDTO> = mutableSetOf(),
) : Identifiable {
   constructor(entity: AccountPayableInvoiceEntity) :
      this(
         id = entity.id,
         vendorNumber = entity.vendor.number,
         vendorName = entity.vendor.name,
         vendorGroup = entity.vendor.vendorGroup?.value,
         invoice = entity.invoice,
         type = entity.type.value,
         status = entity.status.value,
         invoiceDate = entity.invoiceDate,
         entryDate = entity.entryDate,
         invoiceAmount = entity.invoiceAmount,
         discountTaken = entity.discountTaken,
         dueDate = entity.dueDate,
         expenseDate = entity.expenseDate,
         paidAmount = entity.paidAmount,
      )

   override fun myId(): UUID? = id
}
