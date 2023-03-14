package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "GeneralLedgerJournalFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [PageRequestBase::class]
)
@Introspected
class GeneralLedgerJournalPostFilterRequest(

   @field:Schema(name = "beginProfitCenter", description = "Beginning General ledger profit center")
   var beginProfitCenter: Int? = null,

   @field:Schema(name = "endProfitCenter", description = "Ending General ledger profit center")
   var endProfitCenter: Int? = null,

   @field:Schema(name = "beginSourceCode", description = "Beginning General ledger source code")
   var beginSourceCode: String? = null,

   @field:Schema(name = "endSourceCode", description = "End General ledger source code")
   var endSourceCode: String? = null,

   @field:Schema(name = "fromDate", description = "From date for general ledger journal")
   var fromDate: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDate: LocalDate? = null,

   ) : SortableRequestBase<GeneralLedgerJournalPostFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginProfitCenter" to beginProfitCenter,
         "endProfitCenter" to endProfitCenter,
         "beginSourceCode" to beginSourceCode,
         "endSourceCode" to endSourceCode,
         "fromDate" to fromDate,
         "thruDate" to thruDate,
      )

   fun toGeneralLedgerFilterRequest() = GeneralLedgerJournalFilterRequest(
      beginProfitCenter = beginProfitCenter,
      endProfitCenter = endProfitCenter,
      beginSourceCode = beginSourceCode,
      endSourceCode = endSourceCode,
      fromDate = fromDate,
      thruDate = thruDate
   )
}
