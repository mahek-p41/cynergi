package com.cynergisuite.domain

import com.cynergisuite.middleware.localization.MessageCodes.Validation.MAX
import com.cynergisuite.middleware.localization.MessageCodes.Validation.MIN
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Pattern.Flag.CASE_INSENSITIVE

@ValueObject
data class PageRequest(

   @field:NotNull(message = NOT_NULL)
   @field:Min(value = 1, message = MIN)
   var page: Int? = 1,

   @field:NotNull(message = NOT_NULL)
   @field:Min(value = 1, message = MIN)
   @field:Max(value = 100, message = MAX)
   var size: Int? = 10,

   @field:NotNull(message = NOT_NULL)
   @field:Pattern(regexp = "id|name", flags = [CASE_INSENSITIVE])
   var sortBy: String? = "id",

   @field:NotNull(message = NOT_NULL)
   @field:Pattern(regexp = "ASC|DESC", flags = [CASE_INSENSITIVE])
   var sortDirection: String? = "ASC"
) {
   constructor(page: Int, size: Int, sortBy: String, sortDirection: PageRequestSortDirection) :
      this(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection.direction
      )

   override fun toString(): String {
      return "?page=$page&size=$size&sortBy=$sortBy&sortDirection=$sortDirection"
   }

   fun offset(): Int {
      val requestedOffsetPage: Int = page ?: 1
      val offsetPage = requestedOffsetPage - 1
      val offsetSize = size ?: 0

      return offsetPage * offsetSize
   }
}

enum class PageRequestSortDirection(
   val direction: String
) {
   ASCENDING("ASC"),
   DESCENDING("DESC")
}
