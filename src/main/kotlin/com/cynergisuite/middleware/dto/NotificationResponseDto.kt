package com.cynergisuite.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.cynergisuite.middleware.notification.NotificationDto

@Deprecated(message = "Remove this here and in the front end at some point, there shouldn't be any need for this wrapper")
@com.cynergisuite.middleware.dto.DataTransferObject
@JsonInclude(value = JsonInclude.Include.ALWAYS)
data class NotificationResponseDto(
   var notification: NotificationDto
)
