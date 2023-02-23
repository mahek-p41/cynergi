package com.cynergisuite.middleware.sign.here

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.ValidPageSortBy
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime

@Schema(
   name = "DocumentPageRequest",
   title = "How to query for a paged set of documents",
   description = "This is the form of the URL parameters that can be used to query for a subset of a larger dataset. Example: ?page=1&size=10&sortBy=id&sortDirection=ASC",
   allOf = [PageRequestBase::class]
)
@Introspected
class DocumentPageRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "from", description = "Bottom end of the range which will be used to filter audits.  If from is found thru is required.  If both from and thru are empty then the result will include all audits")
   var from: OffsetDateTime? = null,

   @field:Schema(name = "thru", description = "Top end of the range which will be used to filter audits.  If from is found thru is required.  If both from and thru are empty then the result will include all audits")
   var thru: OffsetDateTime? = null,
) : PageRequestBase<DocumentPageRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is DocumentPageRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.from, other.from)
            .append(this.thru, other.thru)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.from)
         .append(this.thru)
         .toHashCode()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): DocumentPageRequest =
      DocumentPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         from = this.from,
         thru = this.thru,
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "from" to from,
         "thru" to thru,
      )
}
