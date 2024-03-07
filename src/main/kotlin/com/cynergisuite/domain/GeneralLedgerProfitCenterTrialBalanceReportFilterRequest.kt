package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@Schema(
   name = "GeneralLedgerProfitCenterTrialBalanceReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
@Introspected
class GeneralLedgerProfitCenterTrialBalanceReportFilterRequest(

   @field:Schema(name = "startingAccount", description = "Starting number of account range")
   var startingAccount: Int? = null,

   @field:Schema(name = "endingAccount", description = "Ending number of account range")
   var endingAccount: Int? = null,

   @field:NotNull
   @field:Schema(name = "selectLocsBy", description = "How locations will be selected")
   var selectLocsBy: Int? = null,

   @field:Schema(name = "any10LocsOrGroups", description = "List of locations or groups when selecting by any 10")
   var any10LocsOrGroups: List<Int>? = null,

   @field:Schema(name = "startingLocOrGroup", description = "Starting loc or group when selecting by range")
   var startingLocOrGroup: Int? = null,

   @field:Schema(name = "endingLocOrGroup", description = "Ending loc or group when selecting by range")
   var endingLocOrGroup: Int? = null,

   @field:NotNull
   @field:Schema(name = "fromDate", description = "From date")
   var fromDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "thruDate", description = "Thru date")
   var thruDate: LocalDate? = null,

   @field:Pattern(regexp = "location|account")
   @field:Schema(description = "The column to sort the GL Profit Center Trial Balance report by (location|account).", defaultValue = "location")
   override var sortBy: String? = "location",

) : SortableRequestBase<GeneralLedgerProfitCenterTrialBalanceReportFilterRequest>("location", "ASC") {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "startingAccount" to startingAccount,
         "endingAccount" to endingAccount,
         "selectLocsBy" to selectLocsBy,
         "any10LocsOrGroups" to any10LocsOrGroups,
         "startingLocOrGroup" to startingLocOrGroup,
         "endingLocOrGroup" to endingLocOrGroup,
         "fromDate" to fromDate,
         "thruDate" to thruDate,
      )
}
