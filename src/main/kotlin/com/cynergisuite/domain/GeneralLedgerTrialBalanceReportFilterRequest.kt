package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Schema(
   name = "GeneralLedgerTrialBalanceReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
@Introspected
class GeneralLedgerTrialBalanceReportFilterRequest(

   @field:Schema(name = "beginAccount", description = "Starting number of account range")
   var beginAccount: Int? = null,

   @field:Schema(name = "endAccount", description = "Ending number of account range")
   var endAccount: Int? = null,

   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: Int? = null,

   @field:NotNull
   @field:Schema(name = "from", description = "From date", required = true)
   var from: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "thru", description = "Thru date", required = true)
   var thru: LocalDate? = null,

   ) : SortableRequestBase<GeneralLedgerTrialBalanceReportFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginAccount" to beginAccount,
         "endAccount" to endAccount,
         "profitCenter" to profitCenter,
         "fromDate" to from,
         "thruDate" to thru,
      )
}
