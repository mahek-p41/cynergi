package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.extensions.sumByBigDecimal
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoiceReportTemplate", title = "Account Payable Invoice Report Template", description = "Account Payable Invoice Report Template")
data class AccountPayableInvoiceReportTemplate(

   @field:Schema(description = "Total of AP Invoice amount for all Invoices on report")
   var expenseTotal: BigDecimal? = null,

   @field:Schema(description = "Total of AP Payment Detail amount for all Invoices on report")
   var paidTotal: BigDecimal? = null,

   @field:Schema(description = "Listing of Invoices")
   var purchaseOrders: List<AccountPayableInvoiceReportPoWrapper>? = null

) {
   constructor(entities: List<AccountPayableInvoiceReportPoWrapper>) :
      this(
         purchaseOrders = entities,
         expenseTotal = entities.flatMap { it.invoices }.mapNotNull { it!!.invoiceAmount }.sumOf { it },
         paidTotal = entities.flatMap { it.invoices }.flatMap { it!!.invoiceDetails }.mapNotNull { it.paymentDetailAmount }.sumOf { it }
      )

   @get:Schema(description = "Account Summary Total")
   val accountSummary get() = purchaseOrders!!.flatMap { it.invoices }.flatMap { it!!.distDetails }
      .groupBy { it.accountNumber }
      .map { (_, items) -> items.first().copy(distAmount = items.sumByBigDecimal { it.distAmount ?: BigDecimal.ZERO }) }
      .sortedBy { it.accountNumber }

}
