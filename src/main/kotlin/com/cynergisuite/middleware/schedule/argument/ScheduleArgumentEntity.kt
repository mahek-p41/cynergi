package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.domain.Identifiable
import java.time.OffsetDateTime

data class ScheduleArgumentEntity(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val value: String,
   val description: String
) : Identifiable {

   constructor(value: String, description: String) :
      this(
         id = null,
         value = value,
         description = description
      )

   override fun myId(): Long? = id
}
