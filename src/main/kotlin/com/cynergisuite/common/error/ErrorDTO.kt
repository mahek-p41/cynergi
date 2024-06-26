package com.cynergisuite.common.error

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import org.apache.commons.lang3.builder.CompareToBuilder

@Introspected
@JsonInclude(NON_NULL)
data class ErrorDTO(
   var message: String,
   var code: String,
   var path: String? = null
) : Comparable<ErrorDTO> {
   override fun compareTo(other: ErrorDTO): Int =
      CompareToBuilder()
         .append(this.message, other.message)
         .append(this.path, other.path)
         .toComparison()
}
