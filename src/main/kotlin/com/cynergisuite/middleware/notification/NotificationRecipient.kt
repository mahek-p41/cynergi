package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.IdentifiableEntity
import java.time.OffsetDateTime
import java.util.Objects
import java.util.UUID

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

   constructor(dto: NotificationRecipientValueObject, notification: IdentifiableEntity) :
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
