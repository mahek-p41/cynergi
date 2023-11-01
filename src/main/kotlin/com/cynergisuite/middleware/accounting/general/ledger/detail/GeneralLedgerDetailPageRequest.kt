package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_PAGE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SIZE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_BY
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_DIRECTION
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Schema(
   name = "GeneralLedgerDetailPageRequest",
   title = "How to query for a paged set of items",
   description = "This is the form of the URL parameters that can be used to query for a subset of a larger dataset. Example: ?page=1&size=10&sortBy=id&sortDirection=ASC",
   allOf = [PageRequestBase::class]
)
class GeneralLedgerDetailPageRequest(
   page: Int? = DEFAULT_PAGE,
   size: Int? = DEFAULT_SIZE,
   sortBy: String? = DEFAULT_SORT_BY,
   sortDirection: String? = DEFAULT_SORT_DIRECTION,
) : StandardPageRequest(page, size, sortBy, sortDirection) {

   @field:NotNull
   @field:Schema(name = "from", description = "Bottom end of the range which will be used to filter GL Details.  If from is found thru is required.  If both from and thru are empty then the result will include all GL Details", required = true)
   var from: LocalDate? = null

   @field:NotNull
   @field:Schema(name = "thru", description = "Top end of the range which will be used to filter GL Details.  If from is found thru is required.  If both from and thru are empty then the result will include all GL Details", required = true)
   var thru: LocalDate? = null

   @field:NotNull
   @field:Schema(name = "account", description = "Account number", required = true)
   var account: Int? = null

   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: Int? = null

   @field:NotNull
   @field:Schema(name = "fiscalYear", description = "Fiscal year", required = true)
   var fiscalYear: Int? = null

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String) =
      GeneralLedgerDetailPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection
      ).also {
         it.from = from
         it.thru = thru
         it.account = account
         it.profitCenter = profitCenter
         it.fiscalYear = fiscalYear
      }

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "from" to from,
         "thru" to thru,
         "account" to account,
         "profitCenter" to profitCenter,
         "fiscalYear" to fiscalYear,
      )
}
