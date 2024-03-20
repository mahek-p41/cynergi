package com.cynergisuite.middleware.accounting.account.payable.expense

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(name = "AccountPayableExpenseReportOverview", title = "Account Payable Expense Report Overview", description = "Account Payable Expense Report Overview")
@JsonPropertyOrder(value = ["vendorNumber", "vendorName", "glAmountTotal" ,"invoices"])
class AccountPayableExpenseReportOverview(
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int,

   @field:Schema(description = "Vendor name")
   var vendorName: String,

   private var periodDateRangeDTO: ClosedRange<LocalDate>,

   @field:Schema(description = "Internal listing of Invoices. Used as data source for getters.")
   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
   var invoices: List<AccountPayableExpenseReportDTO> = mutableListOf(),
) {
   val invoiceOverviews = invoices.map { AccountPayableInvoiceOverviewDTO(it, periodDateRangeDTO) }

   val beginBalance = invoiceOverviews.sumOf { it.beginBalance }

   val newInvoiceAmount = invoiceOverviews.sumOf { it.newInvoiceAmount }

   val paidInvoiceAmount = invoiceOverviews.sumOf { it.paidInvoiceAmount }

   val endBalance = invoiceOverviews.sumOf { it.endBalance }
}
