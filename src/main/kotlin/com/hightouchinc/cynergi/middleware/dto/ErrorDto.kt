package com.hightouchinc.cynergi.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.hightouchinc.cynergi.middleware.domain.DataTransferObject

@JsonInclude(JsonInclude.Include.NON_NULL)
@DataTransferObject
data class ErrorDto(
   val message: String,
   val path: String? = null
)
