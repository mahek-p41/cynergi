package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
   name = "AccountPayableInvoiceFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [PageRequestBase::class]
)
@Introspected
class AccountPayableInvoiceFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "status", description = "Invoice Status")
   var status: String? = null,

   @field:Schema(name = "poNumber", description = "PO Number")
   var poNumber: Int? = null,

   @field:Schema(name = "invoiceNumber", description = "Invoice Number")
   var invoiceNumber: String? = null,

   @field:Schema(name = "invoiceDate", description = "Invoice date")
   var invoiceDate: LocalDate? = null,

   @field:Schema(name = "dueDate", description = "Due date")
   var dueDate: LocalDate? = null,

   @field:Schema(name = "invoiceAmount", description = "Invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:Schema(name = "sortOption", description = "Sort Option")
   var sortOption: String? = null

) : PageRequestBase<AccountPayableInvoiceFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AccountPayableInvoiceFilterRequest =
      AccountPayableInvoiceFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         status = this.status,
         poNumber = this.poNumber,
         invoiceNumber = this.invoiceNumber,
         invoiceDate = this.invoiceDate,
         dueDate = this.dueDate,
         invoiceAmount = this.invoiceAmount,
         sortOption = this.sortOption
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "status" to status,
         "poNumber" to poNumber,
         "invoiceNumber" to invoiceNumber,
         "invoiceDate" to invoiceDate,
         "dueDate" to dueDate,
         "invoiceAmount" to invoiceAmount,
         "sortOption" to sortOption
      )
}
