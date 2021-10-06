package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.domain.Identifiable
import java.util.UUID

data class ScheduleArgumentEntity(
   val id: UUID? = null,
   val value: String,
   val description: String
) : Identifiable {

   constructor(value: String, description: String) :
      this(
         id = null,
         value = value,
         description = description
      )

   override fun myId(): UUID? = id
}
