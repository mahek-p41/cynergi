package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "StagingDepositFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?verifiedSuccessful=true",
   allOf = [SortableRequestBase::class]
)
@Introspected
class StagingDepositFilterRequest(

   @field:Schema(name = "verifiedSuccessful", description = "Verify Successful", required = false)
   var verifiedSuccessful: Boolean? = null,

   @field:Schema(name = "movedToJe", description = "Moved to pending JEs", required = false)
   var movedToJe: Boolean = false,

   @field:Schema(name = "from", description = "Bottom end of the range which will be used to filter GL Details.  If from is found thru is required.  If both from and thru are empty then the result will include all GL Details", required = false)
   var from: LocalDate? = null,

   @field:Schema(name = "thru", description = "Top end of the range which will be used to filter GL Details.  If from is found thru is required.  If both from and thru are empty then the result will include all GL Details", required = false)
   var thru: LocalDate? = null,

   @field:Schema(name = "beginStore", description = "Begin Store", required = false)
   var beginStore: Int? = null,

   @field:Schema(name = "endStore", description = "End Store", required = false)
   var endStore: Int? = null,

) : SortableRequestBase<StagingDepositFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "verifiedSuccessful" to verifiedSuccessful,
         "movedToJe" to movedToJe,
         "from" to from,
         "thru" to thru,
         "beginStore" to beginStore,
         "endStore" to endStore,
      )
}
