package com.cynergisuite.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@com.cynergisuite.middleware.dto.DataTransferObject
data class MessageDto(
   val message: String
)

