package com.cynergisuite.domain

import com.google.common.base.CaseFormat
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Pattern.Flag.CASE_INSENSITIVE

@ValueObject
@Schema(name = "PageRequest", description = "This is the form of the URL parameters that can be used to query for a subset of a larger dataset. Example: ?page=1&size=10&sortBy=id&sortDirection=ASC")
open class PageRequest {

   @field:NotNull
   @field:Min(value = 1)
   @field:Schema(minimum = "1", description = "The page that is requested.  Starts with 1", defaultValue = "1")
   var page: Int = 1

   @field:NotNull
   @field:Min(value = 5)
   @field:Max(value = 100)
   @field:Schema(minimum = "5", description = "How many items for each page", defaultValue = "10")
   var size: Int = 10

   @field:NotNull
   @field:Schema(description = "The column to sort the data by.  Currently only id and name are supported", defaultValue = "id")
   var sortBy: String = "id" // this is open so that child classes can override this property and add specialized sorting

   @field:NotNull
   @field:Pattern(regexp = "ASC|DESC", flags = [CASE_INSENSITIVE])
   @field:Schema(description = "The direction the results should be sorted by.  Either Ascending or Descending", defaultValue = "ASC")
   var sortDirection: String = "ASC"

   constructor()

   constructor(pageRequest: PageRequest) :
      this(
         pageRequest.page,
         pageRequest.size,
         pageRequest.sortBy,
         pageRequest.sortDirection
      )

   constructor(page: Int, size: Int, sortBy: String, sortDirection: String) {
      this.page = page
      this.size = size
      this.sortBy = sortBy
      this.sortDirection = sortDirection
   }

   fun offset(): Int {
      val requestedOffsetPage: Int = page
      val offsetPage = requestedOffsetPage - 1
      val offsetSize = size

      return offsetPage * offsetSize
   }

   @ValidPageSortBy("id")
   open fun sortByMe() : String = sortBy

   fun camelizeSortBy(): String =
      CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sortByMe())

   override fun equals(other: Any?): Boolean =
      if (other is PageRequest) {
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

   override fun toString(): String = "?page=$page&size=$size&sortBy=$sortBy&sortDirection=$sortDirection"
}
