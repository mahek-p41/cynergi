package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.Identifiable
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
   val notification: Identifiable
) : Identifiable {

   constructor(dto: NotificationRecipientValueObject, notification: Identifiable) :
      this(
         id = dto.id,
         recipient = dto.recipient,
         description = dto.description,
         notification = notification
      )

   constructor(recipient: String, description: String, notification: Notification) :
      this(
         id = null,
         recipient = recipient,
         description = description,
         notification = notification
      )

   override fun myId(): Long? = id

   override fun hashCode(): Int = Objects.hashCode(uuRowId)

   override fun equals(other: Any?): Boolean {
      return when {
         this === other -> true
         other is NotificationRecipient -> this.uuRowId == other.uuRowId
         else -> false
      }
   }

  override fun toString(): String {
      return "NotificationRecipient(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, description=$description, recipient='$recipient', notification=${notification.myId()})"
   }
}
