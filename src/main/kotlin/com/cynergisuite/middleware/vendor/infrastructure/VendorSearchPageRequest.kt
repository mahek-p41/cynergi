package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.SearchPageRequest
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "VendorSearchPageRequest",
   title = "Specialized paging for vendor searching result",
   description = "Defines the parameters available to for a paging request to the vendor search endpoint. Example ?page=1&size=10&fuzzy=false&query=Search%20some%20string&active=true",
   allOf = [PageRequestBase::class]
)
class VendorSearchPageRequest(
   page: Int? = null,
   size: Int? = null,
   query: String? = null,
   fuzzy: Boolean? = true
) : SearchPageRequest(page, size, query, fuzzy) {
   var active: Boolean? = null

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): VendorSearchPageRequest =
      VendorSearchPageRequest(
         page = page,
         size = size,
         query = query,
      ).also { it.active = active }

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "active" to active,
         "query" to query
      )
}
