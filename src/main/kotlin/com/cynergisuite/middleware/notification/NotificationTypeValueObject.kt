package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "NotificationType", description = "Describes what type a notification is.  These values are driven from the database and therefore have no fixed listing")
data class NotificationTypeValueObject (

   @field:Positive
   var id: Long? = null,

   val value: String,

   val description: String

) : ValueObjectBase<NotificationTypeValueObject>() {

   constructor(entity: NotificationTypeDomain) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )

   override fun myId(): Long? = id

   override fun copyMe(): NotificationTypeValueObject = copy()
}
