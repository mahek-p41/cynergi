package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "GeneralLedgerProfitCenterTrialBalanceReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
class GeneralLedgerProfitCenterTrialBalanceReportFilterRequest(

   @field:Schema(name = "startingAccount", description = "Starting number of account range")
   var startingAccount: Int? = null,

   @field:Schema(name = "endingAccount", description = "Ending number of account range")
   var endingAccount: Int? = null,

   @field:Schema(name = "selectLocsBy", description = "How locations will be selected")
   var selectLocsBy: Int? = null,

   @field:Schema(name = "locsOrGroups", description = "List or range of locations or groups based on selectLocsBy")
   var locsOrGroups: List<Int>? = null,

   @field:Schema(name = "startingDate", description = "Starting date")
   var startingDate: LocalDate? = null,

   @field:Schema(name = "endingDate", description = "Ending date")
   var endingDate: LocalDate? = null

) : SortableRequestBase<GeneralLedgerProfitCenterTrialBalanceReportFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "startingAccount" to startingAccount,
         "endingAccount" to endingAccount,
         "selectLocsBy" to selectLocsBy,
         "locsOrGroups" to locsOrGroups,
         "startingDate" to startingDate,
         "endingDate" to endingDate
      )
}
