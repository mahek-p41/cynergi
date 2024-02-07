package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableExpenseReportAccountProfitCenterPair", title = "Account Payable Expense Report Account Profit Center Pair", description = "Account Payable Expense Report Account Profit Center Pair")
data class AccountPayableExpenseReportAccountProfitCenterPair(

   @field:Schema(description = "PO header number")
   var poHeaderNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var accountNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account name")
   var accountName: Int? = null,

   @field:NotNull
   @field:Schema(description = "Profit center number")
   var profitCenterNumber: Int? = null,

   @field:Schema(description = "Listing of Expenses")
   var invoices: MutableSet<AccountPayableExpenseReportDTO?> = mutableSetOf(),

   ) {

//   @get:Schema(description = "Total Purchase Order distributions")
//   val totalPoDistributions get() = invoices.flatMap { it!!.distDetails }.filter { it.isAccountForInventory!! }.mapNotNull { it.distAmount }.sumOf { it }

   @get:Schema(description = "Account total")
   val accountTotal get() = invoices.flatMap { it!!.distDetails }.filter { it.isAccountForInventory!! }.mapNotNull { it.distAmount }.sumOf { it }

}
