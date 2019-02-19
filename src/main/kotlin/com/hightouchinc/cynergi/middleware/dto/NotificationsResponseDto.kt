package com.hightouchinc.cynergi.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.hightouchinc.cynergi.middleware.entity.NotificationDto

@Deprecated(message = "Remove this here and in the front end at some point, there shouldn't be any need for this wrapper")
@DataTransferObject
@JsonInclude(value = JsonInclude.Include.ALWAYS)
data class NotificationsResponseDto(
   var notifications: List<NotificationDto>
)
