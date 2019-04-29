package com.cynergisuite.middleware.error

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@JsonInclude(NON_NULL)
@ValueObject
data class ErrorValueObject(
   val message: String,
   val path: String? = null
)
