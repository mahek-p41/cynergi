package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "VendorPageRequest",
   title = "Specialized paging for listing vendors",
   description = "Defines the parameters available to for a paging request to the vendor-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&firstNameMi=Alan&lastName=Smith&search=ASmth",
   allOf = [PageRequestBase::class]
)
class VendorPageRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "search", description = "Fuzzy search string")
   var search: String? = null

) : PageRequestBase<VendorPageRequest>(page, size, sortBy, sortDirection) {

   constructor(pageRequest: VendorPageRequest) :
      this(
         page = pageRequest.page,
         size = pageRequest.size,
         sortBy = pageRequest.sortBy,
         sortDirection = pageRequest.sortDirection,
         search = pageRequest.search
      )

   @ValidPageSortBy("name")
   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): VendorPageRequest =
      VendorPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         search = search
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "search" to search
      )

}
