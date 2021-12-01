package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.LegacyIdentifiable
import com.cynergisuite.middleware.store.Store
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "NotificationRecipient", title = "Employee receiving a Notification", description = "The person who a Notification is intended for")
data class NotificationRecipientValueObject(

   @field:Positive
   var id: Long? = null,

   @field:Size(max = 255)
   val description: String? = null,

   @field:Size(max = 255)
   @field:NotNull
   var recipient: String

) : LegacyIdentifiable {

   constructor(entity: NotificationRecipient) :
      this(
         id = entity.id,
         description = entity.description,
         recipient = entity.recipient
      )

   constructor(description: String, store: Store) :
      this(
         description = description,
         recipient = store.myNumber().toString().padStart(3, '0')
      )

   override fun myId(): Long? = id
}
