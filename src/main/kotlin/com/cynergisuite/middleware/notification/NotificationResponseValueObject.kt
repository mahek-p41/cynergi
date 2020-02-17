package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS
import io.swagger.v3.oas.annotations.media.Schema

@Deprecated(message = "Remove this here and in the front end at some point, there shouldn't be any need for this wrapper")
@ValueObject
@JsonInclude(value = ALWAYS)
@Schema(name = "NotificationResponse", title = "Wrapper around a Notification", description = "Wrapper around a Notification")
data class NotificationResponseValueObject(
   var notification: NotificationValueObject
)
