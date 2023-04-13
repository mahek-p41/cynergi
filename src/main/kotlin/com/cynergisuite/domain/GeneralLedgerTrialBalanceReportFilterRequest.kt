package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@Schema(
   name = "GeneralLedgerTrialBalanceReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
@Introspected
class GeneralLedgerTrialBalanceReportFilterRequest(

   @field:Schema(name = "startingAccount", description = "Starting number of account range")
   var startingAccount: Int? = null,

   @field:Schema(name = "endingAccount", description = "Ending number of account range")
   var endingAccount: Int? = null,

   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: Int? = null,

   @field:NotNull
   @field:Schema(name = "fromDate", description = "From date", required = true)
   var fromDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "thruDate", description = "Thru date", required = true)
   var thruDate: LocalDate? = null,

   @field:Pattern(regexp = "location|account")
   @field:Schema(description = "The column to sort the GL Trial Balance report by (location|account).", defaultValue = "location")
   override var sortBy: String? = null,

) : SortableRequestBase<GeneralLedgerTrialBalanceReportFilterRequest>("location", "ASC") {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "startingAccount" to startingAccount,
         "endingAccount" to endingAccount,
         "profitCenter" to profitCenter,
         "fromDate" to fromDate,
         "thruDate" to thruDate,
      )
}
