package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.TypeDomainEntity
import java.time.OffsetDateTime
import java.util.UUID

data class NotificationTypeDomain (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val value: String,
   val description: String
) : TypeDomainEntity<NotificationTypeDomain> {

   constructor(id: Long, value: String, description: String) :
      this(
         id = id,
         uuRowId = UUID.randomUUID(),
         timeCreated = OffsetDateTime.now(),
         timeUpdated = OffsetDateTime.now(),
         value = value,
         description = description
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): NotificationTypeDomain = copy()

   override fun myValue(): String = value

   override fun myDescription(): String = description
}
