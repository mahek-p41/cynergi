package com.hightouchinc.cynergi.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.domain.DataTransferObject

@JsonInclude(NON_NULL)
@DataTransferObject
data class ErrorDto(
   val message: String,
   val path: String? = null
)
