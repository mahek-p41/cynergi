package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.YearMonth
import javax.validation.constraints.NotNull

@Schema(
   name = "StagingStatusFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?yearMonth=1",
   allOf = [SortableRequestBase::class]
)
@Introspected
class StagingStatusFilterRequest(

   @field:NotNull
   @field:Schema(name = "yearMonth", description = "Year and Month", required = true)
   var yearMonth: YearMonth,

) : SortableRequestBase<StagingStatusFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "yearMonth" to yearMonth,
      )
}
