package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

@Schema(
   name = "AccountPayableCheckPreviewFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
class AccountPayableVendorBalanceReportFilterRequest(

   @field:Schema(name = "beginVendor", description = "Begin Vendor")
   var beginVendor: Int,

   @field:Schema(name = "endVendor", description = "End Vendor")
   var endVendor: Int,

   @field:Schema(name = "fromDate", description = "From date")
   var fromDate: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date")
   var thruDate: LocalDate? = null,

   @field:Schema(name = "sortOption", description = "Sort Option")
   var sortOption: String? = null

) : SortableRequestBase<AccountPayableVendorBalanceReportFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginVendor" to beginVendor,
         "endVendor" to endVendor,
         "fromDate" to fromDate,
         "thruDate" to thruDate,
         "sortOption" to sortOption
      )
}
