package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableRecurringInvoiceReportTemplate", title = "Account Payable Recurring Invoice Report Template", description = "Account Payable Recurring Invoice Report Template")
data class AccountPayableRecurringInvoiceReportTemplate(

   @field:Schema(description = "List of Invoices")
   var invoices: MutableList<AccountPayableRecurringInvoiceReportDTO?> = mutableListOf()

)
