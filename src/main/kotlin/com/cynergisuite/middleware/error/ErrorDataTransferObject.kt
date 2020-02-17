package com.cynergisuite.middleware.error

import com.cynergisuite.domain.DataTransferObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.apache.commons.lang3.builder.CompareToBuilder

@DataTransferObject
@JsonInclude(NON_NULL)
data class ErrorDataTransferObject(
   var message: String,
   var path: String? = null
): Comparable<ErrorDataTransferObject> {
   override fun compareTo(other: ErrorDataTransferObject): Int =
      CompareToBuilder()
         .append(this.message, other.message)
         .append(this.path, other.path)
         .toComparison()
}
