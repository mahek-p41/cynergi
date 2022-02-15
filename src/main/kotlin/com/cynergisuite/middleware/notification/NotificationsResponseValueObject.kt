package com.cynergisuite.middleware.notification

import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema

@Introspected
@Deprecated(message = "Remove this here and in the front end at some point, there shouldn't be any need for this wrapper")
@JsonInclude(value = JsonInclude.Include.ALWAYS)
@Schema(name = "NotificationsResponse", title = "List of Notifications", description = "Wrapper around a list of Notifications")
data class NotificationsResponseValueObject(
   var notifications: List<NotificationValueObject>
)
