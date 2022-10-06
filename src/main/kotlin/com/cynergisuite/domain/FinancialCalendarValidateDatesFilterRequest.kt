package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Schema(
   name = "FinancialCalendarValidateDatesFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
class FinancialCalendarValidateDatesFilterRequest(

   @field:NotNull
   @field:Schema(name = "startingDate", description = "Starting date")
   var startingDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "endingDate", description = "Ending date")
   var endingDate: LocalDate? = null

) : SortableRequestBase<FinancialCalendarValidateDatesFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "startingDate" to startingDate,
         "endingDate" to endingDate
      )
}
