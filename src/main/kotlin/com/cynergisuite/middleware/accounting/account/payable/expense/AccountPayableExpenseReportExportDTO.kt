package com.cynergisuite.middleware.accounting.account.payable.expense

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableExpenseReportExport", title = "Account Payable Expense Report Export", description = "Account Payable Expense Report Export")
data class AccountPayableExpenseReportExportDTO(

   @field:Schema(description = "Account Payable Expense ID")
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
   @field:Schema(description = "Expense")
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

   @field:NotNull
   @field:Schema(description = "Account payable invoice status id")
   var status: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:Schema(description = "Account payable invoice expense date", required = false)
   var expenseDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice amount")
   var paidAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Bank number")
   var bankNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Payment number")
   var pmtNumber: String? = null,

   @field:NotNull
   @field:Schema(description = "Payment date")
   var pmtDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var acctNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account name")
   var acctName: String? = null,

   @field:NotNull
   @field:Schema(description = "Distribution number")
   var distCenter: String? = null,

   @field:NotNull
   @field:Schema(description = "Distribution amount")
   var distAmount: BigDecimal? = null,

) : Identifiable {
   override fun myId(): UUID? = id
}
