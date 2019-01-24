package com.hightouchinc.cynergi.middleware.dto

import com.hightouchinc.cynergi.middleware.domain.DataTransferObject
import com.hightouchinc.cynergi.middleware.entity.NotificationDto

@Deprecated(message = "Remove this here and in the front end at some point, there shouldn't be any need for this wrapper")
@DataTransferObject
data class NotificationsResponseDto(
   var notifications: List<NotificationDto>
)
