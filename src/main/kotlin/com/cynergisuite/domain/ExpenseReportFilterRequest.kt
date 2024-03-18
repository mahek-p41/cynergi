package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@Schema(
   name = "ExpenseReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [SortableRequestBase::class]
)
@Introspected
open class ExpenseReportFilterRequest(

   @field:Schema(name = "beginAcct", description = "Beginning Account number")
   var beginAcct: Int? = null,

   @field:Schema(name = "endAcct", description = "Ending Account number")
   var endAcct: Int? = null,

   @field:Schema(name = "beginVen", description = "Beginning Vendor number")
   var beginVen: Int? = null,

   @field:Schema(name = "endVen", description = "Ending Vendor number")
   var endVen: Int? = null,

   @field:Schema(name = "beginVenGr", description = "Beginning Vendor group number")
   var beginVenGr: String? = null,

   @field:Schema(name = "endVenGr", description = "Ending Vendor group number")
   var endVenGr: String? = null,

   @field:NotNull
   @field:Schema(name = "beginDate", description = "Beginning date", required = true)
   var beginDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "endDate", description = "Ending date", required = true)
   var endDate: LocalDate? = null,

   @field:Schema(name = "iclHoldInv", description = "Include the hold invoices", defaultValue = "false")
   var iclHoldInv: Boolean? = false,

   @field:Schema(name = "invStatus", description = "The Invoice Status to filter results with")
   var invStatus: List<String>? = null,

   @field:Pattern(regexp = "account|vendor")
   @field:Schema(description = "The column to sort the AP Expense report by (account|vendor).", defaultValue = "account")
   override var sortBy: String? = "account",

) : SortableRequestBase<ExpenseReportFilterRequest>(sortBy, "ASC") {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginAcct" to beginAcct,
         "endAcct" to endAcct,
         "beginVen" to beginVen,
         "endVen" to endVen,
         "beginVenGr" to beginVenGr,
         "endVenGr" to endVenGr,
         "beginDate" to beginDate,
         "endDate" to endDate,
         "iclHoldInv" to iclHoldInv,
         "invStatus" to invStatus,
      )
}
