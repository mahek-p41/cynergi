package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class NotificationRecipientValueObject (

   @field:Positive
   var id: Long? = null,

   @field:Size(max = 255)
   val description: String? = null,

   @field:Size(max = 255)
   @field:NotNull
   var recipient: String

) : ValueObjectBase<NotificationRecipientValueObject>() {

   constructor(entity: NotificationRecipient) :
      this(
         id = entity.id,
         description = entity.description,
         recipient = entity.recipient
      )

   override fun valueObjectId(): Long? = id

   override fun copyMe(): NotificationRecipientValueObject = copy()
}
