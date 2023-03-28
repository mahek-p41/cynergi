package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.SortableRequestBase
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "GeneralLedgerDetailFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for purging multiple general ledger detail records.",
   allOf = [PageRequestBase::class]
)
@Introspected
class GeneralLedgerDetailPostPurgeDTO(

   @field:Schema(name = "fromDate", description = "From date for general ledger detail")
   var fromDate: LocalDate,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger detail")
   var thruDate: LocalDate,

   @field:Schema(name = "sourceCode", description = "General ledger source code")
   var sourceCode: String,

   ) : SortableRequestBase<GeneralLedgerDetailPostPurgeDTO>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "fromDate" to fromDate,
         "thruDate" to thruDate,
         "sourceCode" to sourceCode,
      )
}
