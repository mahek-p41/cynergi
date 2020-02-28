package com.cynergisuite.middleware.notification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Deprecated("This just needs to be removed when notifications gets moved to cynergi-client")
@JsonInclude(NON_NULL)
@Schema(name = "NotificationRequest", title = "Wrapper around a single Notification", description = "Wrapper around a single Notification")
data class NotificationRequestValueObject(

   @field:Valid
   @field:NotNull
   var notification: NotificationValueObject
)
