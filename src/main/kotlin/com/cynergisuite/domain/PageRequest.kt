package com.cynergisuite.domain

import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_PAGE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SIZE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_BY
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_DIRECTION
import com.cynergisuite.extensions.isAllSameCase
import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.Pattern
import javax.validation.constraints.Pattern.Flag.CASE_INSENSITIVE

object PageRequestDefaults {
   const val DEFAULT_PAGE: Int = 1
   const val DEFAULT_SIZE: Int = 10
   const val DEFAULT_SORT_BY: String = "id"
   const val DEFAULT_SORT_DIRECTION: String = "ASC"
}

interface PageRequest {
   fun page(): Int
   fun size(): Int
   fun sortBy(): String
   fun sortDirection(): String
   fun snakeSortBy(): String
   fun offset(): Int
   fun nextPage(): PageRequest
}

@DataTransferObject
@Schema(
   name = "PageRequestBase",
   title = "Basic implementation of a page request"
)
abstract class PageRequestBase<out PAGE: PageRequest>(

   @field:Min(value = 1)
   @field:Schema(minimum = "1", description = "The page that is requested.  Starts with 1", defaultValue = "1")
   var page: Int?,

   @field:Min(value = 5)
   @field:Max(value = 100)
   @field:Schema(minimum = "5", description = "How many items for each page", defaultValue = "10")
   var size: Int?,

   @field:Schema(description = "The column to sort the data by.  Currently only id and name are supported", defaultValue = "id")
   var sortBy: String?,

   @field:Pattern(regexp = "ASC|DESC", flags = [CASE_INSENSITIVE])
   @field:Schema(description = "The direction the results should be sorted by.  Either Ascending or Descending", defaultValue = "ASC")
   var sortDirection: String?

) : PageRequest {

   protected abstract fun myNextPage(page: Int, size: Int, sortBy: String, sortDirection: String): PAGE
   protected abstract fun sortByMe(): String
   protected abstract fun myToString(stringBuilder: StringBuilder, separatorIn: String)

   override fun page(): Int = page ?: DEFAULT_PAGE
   override fun size(): Int = size ?: DEFAULT_SIZE
   override fun sortBy(): String = sortBy ?: DEFAULT_SORT_BY
   override fun sortDirection(): String = sortDirection ?: DEFAULT_SORT_DIRECTION
   override fun nextPage(): PAGE = myNextPage(this.page() + 1, size(), sortBy(), sortDirection())

   override fun snakeSortBy(): String {
      val sortByMe = sortByMe()

      return if (sortByMe.isAllSameCase()) {
         sortBy()
      } else {
         LOWER_CAMEL.to(LOWER_UNDERSCORE, sortByMe())
      }
   }

   override fun offset(): Int {
      val requestedOffsetPage: Int = page()
      val offsetPage = requestedOffsetPage - 1
      val offsetSize = size()

      return offsetPage * offsetSize
   }

   override fun equals(other: Any?): Boolean =
      if (other is PageRequestBase<*>) {
         EqualsBuilder()
            .append(this.page, other.page)
            .append(this.size, other.size)
            .append(this.sortBy, other.sortBy)
            .append(this.sortDirection, other.sortDirection)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .append(this.page)
         .append(this.size)
         .append(this.sortBy)
         .append(this.sortDirection)
         .toHashCode()

   override fun toString(): String { //= myToString("?page=$page&size=$size&sortBy=$sortBy&sortDirection=$sortDirection")
      val stringBuilder = StringBuilder()
      var separator = "?"

      separator = page?.apply { stringBuilder.append(separator).append("page=").append(this) }.let { "&" }
      separator = size?.apply { stringBuilder.append(separator).append("size=").append(this) }.let { "&" }
      separator = sortBy?.apply { stringBuilder.append(separator).append("sortBy=").append(this) }.let { "&" }
      separator = sortDirection?.apply { stringBuilder.append(separator).append("sortDirection=").append(this) }.let { "&" }

      myToString(stringBuilder, separator)

      return stringBuilder.toString()
   }
}

@Schema(
   name = "PageRequest",
   title = "How to query for a paged set of items",
   description = "This is the form of the URL parameters that can be used to query for a subset of a larger dataset. Example: ?page=1&size=10&sortBy=id&sortDirection=ASC",
   allOf = [PageRequestBase::class]
)
class StandardPageRequest(
   page: Int, size: Int, sortBy: String, sortDirection: String
) : PageRequestBase<StandardPageRequest>(page, size, sortBy, sortDirection) {

   constructor(pageRequestIn: PageRequest? = null) :
      this(
         page = pageRequestIn?.page() ?: DEFAULT_PAGE,
         size = pageRequestIn?.size() ?: DEFAULT_SIZE,
         sortBy = pageRequestIn?.sortBy() ?: DEFAULT_SORT_BY,
         sortDirection = pageRequestIn?.sortDirection() ?: DEFAULT_SORT_DIRECTION
      )

   override fun myNextPage(page: Int, size: Int, sortBy: String, sortDirection: String): StandardPageRequest =
      StandardPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection
      )

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()
   override fun myToString(stringBuilder: StringBuilder, separatorIn: String) {  }
}
