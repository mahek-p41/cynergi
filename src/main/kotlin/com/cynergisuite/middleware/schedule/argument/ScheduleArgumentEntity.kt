package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.domain.Entity
import java.time.OffsetDateTime
import java.util.UUID

data class ScheduleArgumentEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val value: String,
   val description: String
) : Entity<ScheduleArgumentEntity> {

   constructor(value: String, description: String) :
      this(
         id = null,
         value = value,
         description = description
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ScheduleArgumentEntity = copy()
}
