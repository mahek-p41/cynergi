package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.dto.spi.DataTransferObjectBase
import com.cynergisuite.middleware.entity.Entity
import com.cynergisuite.middleware.entity.IdentifiableEntity
import com.cynergisuite.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import com.cynergisuite.middleware.localization.MessageCodes.Validation.SIZE
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import java.time.OffsetDateTime
import java.util.Objects
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class NotificationRecipient (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val description: String? = null,
   val recipient: String,
   val notification: IdentifiableEntity
) : Entity<NotificationRecipient> {

   constructor(description: String, recipient: String, notification: IdentifiableEntity) :
      this(
         id = null,
         description = description,
         recipient = recipient,
         notification = notification
      )

   constructor(dto: NotificationRecipientDto, notification: IdentifiableEntity) :
      this(
         id = dto.id,
         recipient = dto.recipient,
         description = dto.description,
         notification = notification
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): NotificationRecipient = copy()

   override fun hashCode(): Int = Objects.hashCode(uuRowId)

   override fun equals(other: Any?): Boolean {
      return when {
         this === other -> true
         other is NotificationRecipient -> this.uuRowId == other.uuRowId
         else -> false
      }
   }
  override fun toString(): String {
      return "NotificationRecipient(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, description=$description, recipient='$recipient', notification=${notification.entityId()})"
   }
}

@JsonInclude(NON_NULL)
data class NotificationRecipientDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:Size(max = 255, message = SIZE)
   val description: String? = null,

   @field:Size(max = 255, message = SIZE)
   @field:NotNull(message = NOT_NULL)
   var recipient: String

) : DataTransferObjectBase<NotificationRecipientDto>() {

   constructor(entity: NotificationRecipient) :
      this(
         id = entity.id,
         description = entity.description,
         recipient = entity.recipient
      )

   override fun dtoId(): Long? = id

   override fun copyMe(): NotificationRecipientDto = copy()
}
