package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "GeneralLedgerInquiryFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [SortableRequestBase::class]
)
@Introspected
class GeneralLedgerInquiryFilterRequest(

   @field:Schema(name = "account", description = "Account number")
   var account: Int? = null,

   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: Int? = null,

   @field:Schema(name = "fiscalYear", description = "Fiscal year")
   var fiscalYear: Int? = null,

) : SortableRequestBase<GeneralLedgerInquiryFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "account" to account,
         "profitCenter" to profitCenter,
         "fiscalYear" to fiscalYear
      )
}
