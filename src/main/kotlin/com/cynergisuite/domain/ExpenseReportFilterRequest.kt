package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.Pattern

@Schema(
   name = "ExpenseReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [SortableRequestBase::class]
)
@Introspected
class ExpenseReportFilterRequest(

   @field:Schema(name = "beginAcct", description = "Beginning Account number")
   var beginAcct: Int? = null,

   @field:Schema(name = "endAcct", description = "Ending Account number")
   var endAcct: Int? = null,

   @field:Schema(name = "beginVen", description = "Beginning Vendor number")
   var beginVen: Int? = null,

   @field:Schema(name = "endVen", description = "Ending Vendor number")
   var endVen: Int? = null,

   @field:Schema(name = "beginDate", description = "Beginning date")
   var beginDate: LocalDate? = null,

   @field:Schema(name = "endDate", description = "Ending date")
   var endDate: LocalDate? = null,

   @field:Schema(name = "iclHoldInv", description = "Include the hold invoices", defaultValue = "false")
   var iclHoldInv: Boolean? = false,

   @field:Schema(name = "invStatus", description = "The Invoice Status to filter results with")
   var invStatus: List<String>? = null,

   @field:Pattern(regexp = "apInvoice.invoice|vendor.number")
   @field:Schema(description = "The column to sort the AP Expense report by (apInvoice.invoice|vendor.number).", defaultValue = "apInvoice.invoice")
   override var sortBy: String? = null,

) : SortableRequestBase<ExpenseReportFilterRequest>("apInvoice.invoice", "ASC") {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginAcct" to beginAcct,
         "endAcct" to endAcct,
         "beginVen" to beginVen,
         "endVen" to endVen,
         "beginDate" to beginDate,
         "endDate" to endDate,
         "invStatus" to invStatus,
      )
}
