package com.cynergisuite.middleware.accounting.account.payable.expense

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "AccountPayableExpenseReportOverview", title = "Account Payable Expense Report Overview", description = "Account Payable Expense Report Overview")
@JsonPropertyOrder(value = ["vendorNumber", "vendorName", "glAmountTotal" ,"invoices"])
class AccountPayableExpenseReportOverview(
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int,

   @field:Schema(description = "Vendor name")
   var vendorName: String,

   @field:Schema(description = "Listing of Invoices")
   var invoices: List<AccountPayableExpenseReportDTO> = mutableListOf(),
) {
   @field:Schema(description = "Sum of GL Amount of each Vendor")
   val glAmountTotal: BigDecimal = invoices.sumOf { it.glAmount ?: BigDecimal.ZERO }
}
