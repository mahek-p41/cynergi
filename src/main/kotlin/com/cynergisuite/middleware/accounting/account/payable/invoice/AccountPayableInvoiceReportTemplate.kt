package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoiceReportTemplate", title = "Account Payable Invoice Report Template", description = "Account Payable Invoice Report Template")
data class AccountPayableInvoiceReportTemplate(

   @field:Schema(description = "Total of AP Invoice amount for all Invoices on report")
   var expenseTotal: BigDecimal? = null,
   var paidTotal: BigDecimal? = null,

   @field:Schema(description = "Listing of Invoices")
   var invoices: List<AccountPayableInvoiceReportDTO>? = null

) {
   constructor(entities: List<AccountPayableInvoiceReportDTO>) :
      this(
         invoices = entities,
         expenseTotal = entities.mapNotNull { it.invoiceAmount }.sumOf { it }
      )
}
