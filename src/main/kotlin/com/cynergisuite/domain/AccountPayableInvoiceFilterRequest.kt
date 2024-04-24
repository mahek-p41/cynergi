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

   @field:Schema(name = "vendor", description = "Vendor Number")
   var vendor: Int? = null,

   @field:Schema(name = "payTo", description = "Pay To Number")
   var payTo: Int? = null,

   @field:Schema(name = "invStatus", description = "Invoice Status")
   var invStatus: String? = null,

   @field:Schema(name = "poNbr", description = "PO Number")
   var poNbr: Int? = null,

   @field:Schema(name = "invNbr", description = "Invoice Number")
   var invNbr: String? = null,

   @field:Schema(name = "invDate", description = "Invoice date")
   var invDate: LocalDate? = null,

   @field:Schema(name = "dueDate", description = "Due date", deprecated = true)
   var dueDate: LocalDate? = null,

   @field:Schema(name = "schedDate", description = "Scheduled payment date")
   var schedDate: LocalDate? = null,

   @field:Schema(name = "invAmount", description = "Invoice amount")
   var invAmount: BigDecimal? = null,

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
         vendor = this.vendor,
         payTo = this.payTo,
         invStatus = this.invStatus,
         poNbr = this.poNbr,
         invNbr = this.invNbr,
         invDate = this.invDate,
         dueDate = this.dueDate,
         schedDate = this.schedDate,
         invAmount = this.invAmount,
         sortOption = this.sortOption
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "vendor" to vendor,
         "payTo" to payTo,
         "invStatus" to invStatus,
         "poNbr" to poNbr,
         "invNbr" to invNbr,
         "invDate" to invDate,
         "dueDate" to dueDate,
         "schedDate" to schedDate,
         "invAmount" to invAmount,
         "sortOption" to sortOption
      )
}
