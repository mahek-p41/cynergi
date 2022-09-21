package com.cynergisuite.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import kotlin.math.ceil

@Introspected
@JsonInclude(ALWAYS)
@Schema(name = "Page", title = "Resulting list of a PageRequest", description = "A sub listing or a large result set")
data class Page<I>(

   @field:Schema(name = "elements", description = "The elements up to 100 returned by the query", required = true)
   val elements: List<I> = emptyList(),

   @field:Schema(name = "requested", description = "The page request used when calculating the number of results to return", required = true, implementation = StandardPageRequest::class)
   val requested: PageRequest,

   @field:Schema(name = "totalElements", description = "The total number of elements that can possibly be returned at the time this query was executed", required = true)
   val totalElements: Long,

   @field:Schema(name = "totalPages", description = "The total number of pages that can possibly be returned at the time this query was executed", required = true)
   val totalPages: Long = ceil(totalElements.toDouble() / (requested.size()).toDouble()).toLong(),

   @field:Schema(name = "first", description = "Boolean value to show whether or not this is the first page", required = true)
   val first: Boolean = requested.page() == 1,

   @field:Schema(name = "last", description = "Boolean value to show whether or not this is the last page", required = true)
   val last: Boolean = requested.page().toLong() == totalPages
) {
   fun isNotEmpty(): Boolean = elements.isNotEmpty()

   fun <N> mapElements(mapper: (i: I) -> N): Page<N> =
      Page(
         elements = elements.map(mapper),
         requested = this.requested,
         totalElements = this.totalElements
      )
}
