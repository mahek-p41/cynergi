package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Schema(
   name = "AgingReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
class AgingReportFilterRequest(

   @field:Schema(name = "vendors", description = "The collection of Vendor IDs to filter results with")
   var vendors: Set<UUID>? = emptySet(),

   @field:Schema(name = "agingDate", description = "Aging date")
   var agingDate: LocalDate? = null

) : SortableRequestBase<AgingReportFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "vendors" to vendors,
         "agingDate" to agingDate
      )
}
