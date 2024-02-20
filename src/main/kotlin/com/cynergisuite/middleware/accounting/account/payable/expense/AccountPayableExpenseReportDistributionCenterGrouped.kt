package com.cynergisuite.middleware.accounting.account.payable.expense

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "AccountPayableExpenseReportDistributionCenterGrouped", title = "Account Payable Expense Report Distribution Center Grouped", description = "Account Payable Expense Report Distribution Center Grouped")
class AccountPayableExpenseReportDistributionCenterGrouped(
   @field:Schema(description = "Distribution center")
   var distCenter: Int,

   @field:Schema(description = "Total of GL Amount for payments")
   var glAmountTotalPerPayment: Map<String, BigDecimal> = mutableMapOf(),

   @field:Schema(description = "Listing of Invoices")
   var invoices: List<AccountPayableExpenseReportDTO?> = mutableListOf(),
) {
   @field:Schema(description = "Sum of GL Amount of each Distribution Center")
   val accountTotal: BigDecimal = invoices.sumOf { it!!.glAmount ?: BigDecimal.ZERO }
}
