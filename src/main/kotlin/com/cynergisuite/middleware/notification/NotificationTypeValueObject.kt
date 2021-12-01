package com.cynergisuite.middleware.notification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "NotificationType", description = "Describes what type a notification is.  These values are driven from the database and therefore have no fixed listing")
data class NotificationTypeValueObject(

   @field:Positive
   var id: Int? = null,

   val value: String,

   val description: String

) {

   constructor(entity: NotificationType) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )

   fun myId(): Int? = id
}
