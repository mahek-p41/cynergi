package com.cynergisuite.domain

import com.cynergisuite.domain.SortingRequestDefaults.DEFAULT_SORT_BY
import com.cynergisuite.domain.SortingRequestDefaults.DEFAULT_SORT_DIRECTION
import com.cynergisuite.extensions.isAllSameCase
import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import javax.validation.constraints.Pattern
import javax.validation.constraints.Pattern.Flag.CASE_INSENSITIVE

object SortingRequestDefaults {
   const val DEFAULT_SORT_BY: String = "id"
   const val DEFAULT_SORT_DIRECTION: String = "ASC"
}

interface SortableRequest {
   fun sortBy(): String
   fun sortDirection(): String
   fun snakeSortBy(): String
}

@Schema(
   name = "SortableRequestBase",
   title = "Basic implementation of a page request"
)
abstract class SortableRequestBase<out PAGE : SortableRequest>(

   @field:Schema(description = "The column to sort the data by.  Currently only id and name are supported", defaultValue = "id")
   open var sortBy: String?,

   @field:Pattern(regexp = "ASC|DESC", flags = [CASE_INSENSITIVE])
   @field:Schema(description = "The direction the results should be sorted by.  Either Ascending or Descending", defaultValue = "ASC")
   var sortDirection: String?

) : SortableRequest {

   protected abstract fun sortByMe(): String
   protected abstract fun myToStringValues(): List<Pair<String, Any?>>

   final override fun sortBy(): String = sortBy ?: DEFAULT_SORT_BY
   final override fun sortDirection(): String = sortDirection?.uppercase() ?: DEFAULT_SORT_DIRECTION

   final override fun snakeSortBy(): String {
      val sortByMe = sortByMe()

      return if (sortByMe.isAllSameCase()) {
         sortBy().lowercase()
      } else {
         LOWER_CAMEL.to(LOWER_UNDERSCORE, sortByMe()).lowercase()
      }
   }

   override fun equals(other: Any?): Boolean =
      if (other is SortableRequestBase<*>) {
         EqualsBuilder()
            .append(this.sortBy, other.sortBy)
            .append(this.sortDirection, other.sortDirection)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .append(this.sortBy)
         .append(this.sortDirection)
         .toHashCode()

   final override fun toString(): String { // = myToString("?page=$page&size=$size&sortBy=$sortBy&sortDirection=$sortDirection")
      val stringBuilder = StringBuilder()
      var separator = "?"

      separator = sortBy?.apply { stringBuilder.append(separator).append("sortBy=").append(this) }?.let { "&" } ?: separator
      separator = sortDirection?.apply { stringBuilder.append(separator).append("sortDirection=").append(this) }?.let { "&" } ?: separator

      myToStringValues().asSequence()
         .filter { (_, value) -> value != null }
         .flatMap { (key, value) ->
            if (value is Iterable<*>) {
               value.asSequence().map { key to it }
            } else {
               sequenceOf(key to value)
            }
         }
         .forEach { (key, value) ->
            separator = value.apply { stringBuilder.append(separator).append(key).append('=').append(this) }.let { "&" }
         }

      return stringBuilder.toString()
   }
}
