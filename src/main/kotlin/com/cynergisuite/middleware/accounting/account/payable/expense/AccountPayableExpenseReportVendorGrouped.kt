package com.cynergisuite.middleware.accounting.account.payable.expense

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "AccountPayableExpenseReportVendorGrouped", title = "Account Payable Expense Report Vendor Grouped", description = "Account Payable Expense Report Vendor Grouped")
class AccountPayableExpenseReportVendorGrouped(
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int,

   @field:Schema(description = "Internal listing of Invoices. Used as data source for getters.")
   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
   var invoices: List<AccountPayableExpenseReportDTO?> = mutableListOf(),
) {
   @get:Schema(description = "Listing of Invoices grouped by Accounts")
   val groupedByAccount get() = invoices
      .groupBy { it!!.acctNumber }
      .map { (accountNumber, list) ->
         val sortedList = list.sortedWith(compareBy<AccountPayableExpenseReportDTO?> {
            it!!.pmtNumber
         }.thenBy {
            it!!.distCenter
         })

         val glAmountTotalPerPayment = list.filterNotNull()
            .filter { it.pmtNumber != null }
            .groupBy { it.pmtNumber!! }
            .filterValues { it.size > 1 }
            .mapValues {
               it.value.sumOf { invoice ->
                  invoice.glAmount ?: BigDecimal.ZERO
               }
            }.toSortedMap()

         AccountPayableExpenseReportLevel2AccountGrouped(accountNumber!!, list.first()!!.acctName, glAmountTotalPerPayment, sortedList)
      }.sortedBy { it.accountNumber }
}
