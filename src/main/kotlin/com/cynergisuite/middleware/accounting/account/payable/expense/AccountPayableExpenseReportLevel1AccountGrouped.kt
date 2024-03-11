package com.cynergisuite.middleware.accounting.account.payable.expense

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AccountPayableExpenseReportLevel1AccountGrouped", title = "Account Payable Expense Report Level 1 Account Grouped", description = "Account Payable Expense Report Level 1 Account Grouped")
@JsonPropertyOrder(value = ["accountNumber", "accountName", "glAmountTotal" ,"groupedByDistributionCenters"])
class AccountPayableExpenseReportLevel1AccountGrouped(
   @field:NotNull
   @field:Schema(description = "Account number")
   var accountNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account name")
   var accountName: String? = null,

   @field:Schema(description = "Internal listing of Invoices. Used as data source for getters.")
   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
   var invoices: List<AccountPayableExpenseReportDTO> = mutableListOf(),
) {
   @get:Schema(description = "Listing of Invoices grouped by Distribution Centers")
   val groupedByDistributionCenters get() = invoices
      .groupBy { it.distCenter }
      .map { (distCenter, list) ->
         val (sortedList, glAmountTotalPerPayment) = AccountPayableExpenseReportTemplate.calculateSortedListAndTotalPerPayment(list)
         AccountPayableExpenseReportDistributionCenterGrouped(distCenter!!, glAmountTotalPerPayment, sortedList)
      }.sortedBy { it.distCenter }

   @field:Schema(description = "Sum of GL Amount of each Account")
   val glAmountTotal: BigDecimal = invoices.sumOf { it.glAmount ?: BigDecimal.ZERO }
}
