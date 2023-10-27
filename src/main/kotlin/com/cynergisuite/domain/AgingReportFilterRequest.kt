package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "AgingReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
class AgingReportFilterRequest(

   @field:Schema(name = "vendorStart", description = "Beginning number of vendor range")
   var vendorStart: Int? = null,

   @field:Schema(name = "vendorEnd", description = "End number of vendor range")
   var vendorEnd: Int? = null,

   @field:Schema(name = "agingDate", description = "Aging date")
   var agingDate: LocalDate? = null

) : SortableRequestBase<AgingReportFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "vendorStart" to vendorStart,
         "vendorEnd" to vendorEnd,
         "agingDate" to agingDate
      )
}
