package com.cynergisuite.middleware.error

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.apache.commons.lang3.builder.CompareToBuilder

@ValueObject
@JsonInclude(NON_NULL)
data class ErrorValueObject(
   var message: String,
   var path: String? = null
): Comparable<ErrorValueObject> {
   override fun compareTo(other: ErrorValueObject): Int =
      CompareToBuilder()
         .append(this.message, other.message)
         .append(this.path, other.path)
         .toComparison()
}
