package com.cynergisuite.middleware.error

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@ValueObject
@JsonInclude(NON_NULL)
data class ErrorValueObject(
   var message: String,
   var path: String? = null
)
