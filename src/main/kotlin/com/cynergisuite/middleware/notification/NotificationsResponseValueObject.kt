package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude

@Deprecated(message = "Remove this here and in the front end at some point, there shouldn't be any need for this wrapper")
@ValueObject
@JsonInclude(value = JsonInclude.Include.ALWAYS)
data class NotificationsResponseValueObject(
   var notifications: List<NotificationValueObject>
)
