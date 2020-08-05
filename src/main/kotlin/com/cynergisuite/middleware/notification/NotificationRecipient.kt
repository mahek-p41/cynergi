package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.Identifiable
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE
import java.time.OffsetDateTime

data class NotificationRecipient(
   val id: Long? = null,
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

   override fun hashCode(): Int =
      HashCodeBuilder()
         .append(id)
         .append(description)
         .append(recipient)
         .append(notification.myId())
         .toHashCode()

   override fun equals(other: Any?): Boolean {
      return when (other) {
         this === other -> true
         is NotificationRecipient ->
            EqualsBuilder()
               .append(this.id, other.id)
               .append(this.description, other.description)
               .append(this.recipient, other.recipient)
               .append(this.notification.myId(), other.notification.myId())
               .build()
         else -> false
      }
   }

   override fun toString(): String =
      ToStringBuilder(this, SIMPLE_STYLE)
         .append(id)
         .append(description)
         .append(recipient)
         .append(notification.myId())
         .build()
}
