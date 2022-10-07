package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.SortableRequestBase
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull


@Schema(
   name = "GeneralLedgerDetailFilterRequest",
   title = "Resulting net change for GL Details",
   description = "This is the form of the URL parameters that can be used to query for net change of GL Details. Example: ?account=1&profitCenter=2",
   allOf = [SortableRequestBase::class]
)
@Introspected
class GeneralLedgerDetailFilterRequest(

   @field:NotNull
   @field:Schema(name = "from", description = "Bottom end of the range which will be used to filter GL Details.  If from is found thru is required.  If both from and thru are empty then the result will include all GL Details")
   var from: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "thru", description = "Top end of the range which will be used to filter GL Details.  If from is found thru is required.  If both from and thru are empty then the result will include all GL Details")
   var thru: LocalDate? = null,

   @field:Schema(name = "account", description = "Account number")
   var account: Int? = null,

   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: Int? = null,

   ) : SortableRequestBase<GeneralLedgerDetailFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "from" to from,
         "thru" to thru,
         "account" to account,
         "profitCenter" to profitCenter,
      )
}
