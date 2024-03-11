package com.cynergisuite.middleware.accounting.account.payable.expense

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "AccountPayableExpenseReportVendorGrouped", title = "Account Payable Expense Report Vendor Grouped", description = "Account Payable Expense Report Vendor Grouped")
@JsonPropertyOrder(value = ["vendorNumber", "vendorName", "glAmountTotal" ,"groupedByAccount"])
class AccountPayableExpenseReportVendorGrouped(
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int,

   @field:Schema(description = "Vendor name")
   var vendorName: String,

   @field:Schema(description = "Internal listing of Invoices. Used as data source for getters.")
   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
   var invoices: List<AccountPayableExpenseReportDTO> = mutableListOf(),
) {
   @field:Schema(description = "Sum of GL Amount of each Vendor")
   val glAmountTotal: BigDecimal = invoices.sumOf { it.glAmount ?: BigDecimal.ZERO }

   @get:Schema(description = "Listing of Invoices grouped by Accounts")
   val groupedByAccount get() = invoices
      .groupBy { it.acctNumber }
      .map { (accountNumber, list) ->
         val (sortedList, glAmountTotalPerPayment) = AccountPayableExpenseReportTemplate.calculateSortedListAndTotalPerPayment(list)
         AccountPayableExpenseReportLevel2AccountGrouped(accountNumber!!, list.first().acctName, glAmountTotalPerPayment, sortedList)
      }.sortedBy { it.accountNumber }
}
