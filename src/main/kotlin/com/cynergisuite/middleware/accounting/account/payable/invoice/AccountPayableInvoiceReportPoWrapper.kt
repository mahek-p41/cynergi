package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoiceReportPoWrapper", title = "Account Payable Invoice Report PO Wrapper", description = "Account Payable Invoice Report PO Wrapper")
data class AccountPayableInvoiceReportPoWrapper(

   @field:NotNull
   @field:Schema(description = "PO header number")
   var poHeaderNumber: Int? = null,

   @field:Schema(description = "Listing of Invoices")
   var invoices: MutableSet<AccountPayableInvoiceReportDTO?> = mutableSetOf(),

   ) {

   @get:Schema(description = "Total Purchase Order inventory cost")
   val totalPoInventoryCost get() = invoices.flatMap { it!!.inventories }.mapNotNull { it.cost }.sumOf { it }

   @get:Schema(description = "Total Purchase Order distributions")
   val totalPoDistributions get() = invoices.flatMap { it!!.distDetails }.filter { it.isAccountForInventory!! }.mapNotNull { it.distAmount }.sumOf { it }
}