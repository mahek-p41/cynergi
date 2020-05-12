package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "SearchPageRequest",
   title = "Specialized paging for searching result",
   description = "Defines the parameters available to for a paging request to the all search endpoint. Example ?page=1&size=10&query=Search%20some%20string",
   allOf = [PageRequestBase::class]
)
class SearchPageRequest(
   page: Int? = null,
   size: Int? = null,

   @field:Schema(name = "query", description = "Search string")
   var query: String? = null

) : PageRequestBase<SearchPageRequest>(page, size, null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): SearchPageRequest =
      SearchPageRequest(
         page = page,
         size = size,
         query = query
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "query" to query
      )
}
