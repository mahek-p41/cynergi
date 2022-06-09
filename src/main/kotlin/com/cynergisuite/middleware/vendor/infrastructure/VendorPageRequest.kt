package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_PAGE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SIZE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_BY
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_DIRECTION
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "VendorPageRequest",
   title = "How to query for a paged set of items",
   description = "This is the form of the URL parameters that can be used to query for a subset of a larger dataset. Example: ?page=1&size=10&sortBy=id&sortDirection=ASC",
   allOf = [PageRequestBase::class]
)
class VendorPageRequest(
   page: Int? = DEFAULT_PAGE,
   size: Int? = DEFAULT_SIZE,
   sortBy: String? = DEFAULT_SORT_BY,
   sortDirection: String? = DEFAULT_SORT_DIRECTION,
) : StandardPageRequest(page, size, sortBy, sortDirection) {
   var active: Boolean? = null

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()
   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "active" to active,
      )
}
