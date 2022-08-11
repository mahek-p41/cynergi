package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(
   name = "GeneralLedgerSourceReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [SortableRequestBase::class]
)
@Introspected
class GeneralLedgerSourceReportFilterRequest(

   @field:Schema(name = "startSource", description = "Starting source code")
   var startSource: String? = null,

   @field:Schema(name = "endSource", description = "Ending source code")
   var endSource: String? = null,

   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: Int? = null,

   @field:Schema(name = "startDate", description = "Starting date")
   var startDate: OffsetDateTime? = null,

   @field:Schema(name = "endDate", description = "Ending date")
   var endDate: OffsetDateTime? = null,

   @field:Schema(name = "jeNumber", description = "Journal entry number")
   var jeNumber: Int? = null,

) : SortableRequestBase<GeneralLedgerSourceReportFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "startSource" to startSource,
         "endSource" to endSource,
         "profitCenter" to profitCenter,
         "startDate" to startDate,
         "endDate" to endDate,
         "jeNumber" to jeNumber,
      )
}
