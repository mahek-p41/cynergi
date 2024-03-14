package com.cynergisuite.middleware.accounting.account.payable.expense

import com.cynergisuite.util.APInvoiceReportOverviewType
import com.cynergisuite.util.GroupingType
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.SortedMap

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

   @field:Schema(description = "Total of AP Expense amount for Invoices without payment information")
   var proformaInvoiceTotal: BigDecimal? = null,

   @field:Schema(description = "Grouping Type")
   var groupingType: GroupingType,

   @field:Schema(description = "Report Overview Type")
   var overviewType: APInvoiceReportOverviewType? = APInvoiceReportOverviewType.SUMMARIZED,

   @field:Schema(description = "Internal listing of Invoices. Used as data source for getters.")
   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
   var invoices: List<AccountPayableExpenseReportDTO> = mutableListOf()

) {
   constructor(
      entities: List<AccountPayableExpenseReportDTO>,
      beginBalance: BigDecimal,
      newInvoicesTotal: BigDecimal,
      paidInvoicesTotal: BigDecimal,
      endBalance: BigDecimal,
      chargedAfterEndingDate: BigDecimal,
      proformaInvoiceTotal: BigDecimal,
      groupingType: GroupingType
   ) : this(
      invoices = entities,
      expenseTotal = entities.mapNotNull { it.invoiceAmount }.sumOf { it },
      paidTotal = entities.mapNotNull { it.paidAmount }.sumOf { it },
      beginBalance = beginBalance,
      newInvoicesTotal = newInvoicesTotal,
      paidInvoicesTotal = paidInvoicesTotal,
      endBalance = endBalance,
      chargedAfterEndingDate = chargedAfterEndingDate,
      proformaInvoiceTotal = proformaInvoiceTotal,
      groupingType = groupingType
   )

   @get:Schema(description = "Total debit amount of all Invoices")
   val debitTotal get() = invoices.filter { it.glAmount != null && it.glAmount!! > BigDecimal.ZERO }.mapNotNull { it.glAmount }.sumOf { it }

   @get:Schema(description = "Total credit amount of all Invoices")
   val creditTotal get() = invoices.filter { it.glAmount != null && it.glAmount!! < BigDecimal.ZERO }.mapNotNull { it.glAmount }.sumOf { it }

   @get:Schema(description = "Listing of Invoices grouped by Accounts")
   val groupedByAccount get() = groupingType.takeIf { it == GroupingType.ACCOUNT }?.let {
      invoices
         .groupBy { it.acctNumber }
         .map { (accountNumber, list) ->
            val sortedList = list.sortedWith(compareBy<AccountPayableExpenseReportDTO?> {
               it!!.pmtNumber
            }.thenBy {
               it!!.distCenter
            })

            AccountPayableExpenseReportLevel1AccountGrouped(accountNumber!!, list.first().acctName, sortedList)
         }.sortedBy { it.accountNumber }
   }

   @get:Schema(description = "Listing of Invoices grouped by Vendors")
   val groupedByVendor get() = groupingType.takeIf { it == GroupingType.VENDOR }?.let {
      invoices
         .groupBy { it.vendorNumber }
         .map { (vendorNumber, list) ->
            val sortedList = list.sortedWith(compareBy<AccountPayableExpenseReportDTO?> {
               it!!.pmtNumber
            }.thenBy {
               it!!.distCenter
            })

            AccountPayableExpenseReportVendorGrouped(vendorNumber!!, list.first().vendorName!!, sortedList)
         }.sortedBy { it.vendorNumber }
   }

   @get:Schema(description = "Report overview")
   val overview get() = overviewType.takeIf { it == APInvoiceReportOverviewType.DETAILED }?.let {
      invoices
         .groupBy { it.vendorNumber }
         .map { (vendorNumber, list) ->
            val sortedList = list.sortedWith(compareBy<AccountPayableExpenseReportDTO?> {
               it!!.pmtNumber
            }.thenBy {
               it!!.distCenter
            })

            AccountPayableExpenseReportOverview(vendorNumber!!, list.first().vendorName!!, sortedList)
         }.sortedBy { it.vendorNumber }
   }

   companion object {
      fun calculateSortedListAndTotalPerPayment(invoices: List<AccountPayableExpenseReportDTO>): Pair<List<AccountPayableExpenseReportDTO>, SortedMap<String, BigDecimal>> {
         val glAmountTotalPerPayment = invoices
            .filter { it.pmtNumber != null }
            .groupBy { it.pmtNumber!! }
            .filterValues { it.size > 1 }
            .mapValues {
               it.value.sumOf { invoice ->
                  invoice.glAmount ?: BigDecimal.ZERO
               }
            }.toSortedMap()

         val previousProcessedPmtNumbers = mutableSetOf<String>()
         val sortedList: List<AccountPayableExpenseReportDTO> = invoices
            .sortedWith(
               compareBy<AccountPayableExpenseReportDTO> { it.expenseDate }
                  .thenBy { it.vendorNumber }
            )
            .flatMap { element ->
               if (element.pmtNumber != null) {
                  // The condition to avoid duplicate elements with same pmtNumber of the sort
                  if (previousProcessedPmtNumbers.find { it == element.pmtNumber } != null) {
                     listOf<AccountPayableExpenseReportDTO>()
                  } else if (glAmountTotalPerPayment.containsKey(element.pmtNumber)) {
                     previousProcessedPmtNumbers.add(element.pmtNumber!!)

                     invoices
                        .filter { it.pmtNumber == element.pmtNumber }
                        .sortedWith(
                           compareBy<AccountPayableExpenseReportDTO> { it.expenseDate }
                              .thenBy { it.vendorNumber }
                        )
                  } else {
                     listOf(element)
                  }
               } else {
                  listOf(element)
               }
            }

         return sortedList to glAmountTotalPerPayment
      }
   }
}
