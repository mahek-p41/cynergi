package com.cynergisuite.middleware.accounting.account.payable.expense

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableExpenseReportLevel2AccountGrouped", title = "Account Payable Expense Report Level 2 Account Grouped", description = "Account Payable Expense Report Level 2 Account Grouped")
data class AccountPayableExpenseReportLevel2AccountGrouped(
   @field:NotNull
   @field:Schema(description = "Account number")
   var accountNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account name")
   var accountName: String? = null,

   @field:Schema(description = "Total of GL Amount per Payment")
   var glAmountTotalPerPayment: Map<String, BigDecimal> = mutableMapOf(),

   @field:Schema(description = "Listing of Invoices")
   var invoices: List<AccountPayableExpenseReportDTO?> = mutableListOf(),
) {
   @field:Schema(description = "Sum of GL Amount of each Account")
   val accountTotal: BigDecimal = invoices.sumOf { it!!.glAmount ?: BigDecimal.ZERO }
}
