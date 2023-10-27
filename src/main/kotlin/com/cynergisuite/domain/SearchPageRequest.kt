package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Schema(
   name = "SearchPageRequest",
   title = "Specialized paging for searching result",
   description = "Defines the parameters available to for a paging request to the all search endpoint. Example ?page=1&size=10&query=Search%20some%20string",
   allOf = [PageRequestBase::class]
)
open class SearchPageRequest(
   page: Int? = null,
   size: Int? = null,

   @field:NotNull
   @field:NotBlank
   @field:Schema(name = "query", description = "Search string")
   var query: String? = null,

   @field:Schema(name = "fuzzy", description = "If the query should be a fuzzy match or an exact one", defaultValue = "true")
   var fuzzy: Boolean? = true

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
