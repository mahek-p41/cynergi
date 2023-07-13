package com.cynergisuite.middleware.accounting.general.ledger.deposit

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
   name = "StagingDepositPageRequest",
   title = "How to query for a paged set of items",
   description = "This is the form of the URL parameters that can be used to query for a subset of a larger dataset. Example: ?page=1&size=10&sortBy=id&sortDirection=ASC",
   allOf = [PageRequestBase::class]
)
class StagingDepositPageRequest(
   page: Int? = DEFAULT_PAGE,
   size: Int? = DEFAULT_SIZE,
   sortBy: String? = DEFAULT_SORT_BY,
   sortDirection: String? = DEFAULT_SORT_DIRECTION,
) : StandardPageRequest(page, size, sortBy, sortDirection) {

   @field:NotNull
   @field:Schema(name = "verifiedSuccessful", description = "Verify Successful", required = true)
   var verifiedSuccessful: Boolean? = null

   @field:Schema(name = "movedToJe", description = "Moved to pending JEs", required = false)
   var movedToJe: Boolean = false

   @field:Schema(name = "from", description = "Bottom end of the range which will be used to filter GL Details.  If from is found thru is required.  If both from and thru are empty then the result will include all GL Details", required = false)
   var from: LocalDate? = null

   @field:Schema(name = "thru", description = "Top end of the range which will be used to filter GL Details.  If from is found thru is required.  If both from and thru are empty then the result will include all GL Details", required = false)
   var thru: LocalDate? = null

   @field:Schema(name = "beginStore", description = "Begin Store", required = false)
   var beginStore: Int? = null

   @field:Schema(name = "endStore", description = "End Store", required = false)
   var endStore: Int? = null

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String) =
      StagingDepositPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection
      ).also {
         it.verifiedSuccessful = verifiedSuccessful
         it.movedToJe = movedToJe
         it.from = from
         it.thru = thru
         it.beginStore = beginStore
         it.endStore = endStore
      }

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
