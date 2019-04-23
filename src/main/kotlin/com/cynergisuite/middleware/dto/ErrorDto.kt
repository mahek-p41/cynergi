package com.cynergisuite.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@JsonInclude(NON_NULL)
@com.cynergisuite.middleware.dto.DataTransferObject
data class ErrorDto(
   val message: String,
   val path: String? = null
)
