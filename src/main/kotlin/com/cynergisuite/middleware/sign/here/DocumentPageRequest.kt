package com.cynergisuite.middleware.sign.here

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_PAGE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SIZE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_BY
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_DIRECTION
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.ValidPageSortBy
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import javax.validation.constraints.PastOrPresent

@Introspected
@Schema(
   name = "DocumentPageRequest",
   title = "How to query for a paged set of documents",
   description = "This is the form of the URL parameters that can be used to query for a subset of a larger dataset. Example: ?page=1&size=10&sortBy=id&sortDirection=ASC",
   allOf = [PageRequestBase::class]
)
class DocumentPageRequest(
   page: Int? = DEFAULT_PAGE,
   size: Int? = DEFAULT_SIZE,
   sortBy: String? = DEFAULT_SORT_BY,
   sortDirection: String? = DEFAULT_SORT_DIRECTION,
) : StandardPageRequest(page, size, sortBy, sortDirection) {
   var org: String? = null
   var store: String? = null
   @PastOrPresent
   var from: OffsetDateTime? = null
   var thru: OffsetDateTime? = null

   @ValidPageSortBy("id", "timeCreated")
   override fun sortByMe(): String =
      when(sortBy().lowercase()) {
         "id" -> "primaryPosition"
         else -> sortBy()
      }

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "org" to org,
         "store" to store,
         "from" to from,
         "thru" to thru,
      )

   override fun toPageable(): Pageable {
      val me = this

      return object : Pageable {
         override fun getNumber(): Int = me.page() - 1
         override fun getSize(): Int = me.size()
         override fun getSort(): Sort {
            return Sort.of(Sort.Order(sortByMe(), toSortDirection(), false))
         }
      }
   }
}
