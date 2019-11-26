package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Deprecated(message = "Remove this here and in the front end at some point, there shouldn't be any need for this wrapper")
@ValueObject
@JsonInclude(value = JsonInclude.Include.ALWAYS)
@Schema(name = "NotificationsResponse", title = "List of Notifications", description = "Wrapper around a list of Notifications")
data class NotificationsResponseValueObject(
   var notifications: List<NotificationValueObject>
)
