package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.extensions.sumByBigDecimal
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableExpenseReportTemplate", title = "Account Payable Expense Report Template", description = "Account Payable Expense Report Template")
data class AccountPayableExpenseReportTemplate(

   @field:Schema(description = "Beginning balance")
   var beginBalance: BigDecimal? = null,

   @field:Schema(description = "New invoices total")
   var newInvoicesTotal: BigDecimal? = null,

   @field:Schema(description = "Paid invoices total")
   var paidInvoicesTotal: BigDecimal? = null,

   @field:Schema(description = "End balance")
   var endBalance: BigDecimal? = null,

   @field:Schema(description = "Charged after ending date total")
   var chargedAfterEndingDate: BigDecimal? = null,

   @field:Schema(description = "Total of AP Expense amount for all Expenses on report")
   var expenseTotal: BigDecimal? = null,

   @field:Schema(description = "Total of AP Payment Detail amount for all Expenses on report")
   var paidTotal: BigDecimal? = null,

   @field:Schema(description = "Listing of Expenses")
   var purchaseOrders: List<AccountPayableExpenseReportAccountProfitCenterPair>? = null

) {
   constructor(entities: List<AccountPayableExpenseReportAccountProfitCenterPair>) :
      this(
         purchaseOrders = entities,
         expenseTotal = entities.flatMap { it.invoices }.mapNotNull { it!!.invoiceAmount }.sumOf { it },
         paidTotal = entities.flatMap { it.invoices }.flatMap { it!!.invoiceDetails }.mapNotNull { it.paymentDetailAmount }.sumOf { it },
      )

   constructor(
      entities: List<AccountPayableExpenseReportAccountProfitCenterPair>,
      beginBalance: BigDecimal,
      newInvoicesTotal: BigDecimal,
      paidInvoicesTotal: BigDecimal,
      endBalance: BigDecimal,
      chargedAfterEndingDate: BigDecimal
   ) : this(
      purchaseOrders = entities,
      expenseTotal = entities.flatMap { it.invoices }.mapNotNull { it!!.invoiceAmount }.sumOf { it },
      paidTotal = entities.flatMap { it.invoices }.flatMap { it!!.invoiceDetails }.mapNotNull { it.paymentDetailAmount }.sumOf { it },
      beginBalance = beginBalance,
      newInvoicesTotal = newInvoicesTotal,
      paidInvoicesTotal = paidInvoicesTotal,
      endBalance = endBalance,
      chargedAfterEndingDate = chargedAfterEndingDate
   )

   @get:Schema(description = "Account Summary Total")
   val accountSummary get() = purchaseOrders!!.flatMap { it.invoices }.flatMap { it!!.distDetails }
      .groupBy { it.accountNumber }
      .map { (_, items) -> items.first().copy(distAmount = items.sumByBigDecimal { it.distAmount ?: BigDecimal.ZERO }) }
      .sortedBy { it.accountNumber }

}
