package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.domain.Identifiable
import java.util.UUID

data class ScheduleArgumentEntity(
   val id: UUID? = null,
   val value: String,
   val description: String,
   val encrypted: Boolean = false,
) : Identifiable {

   constructor(value: String, description: String, encrypted: Boolean = false) :
      this(
         id = null,
         value = value,
         description = description,
         encrypted = encrypted,
      )

   override fun myId(): UUID? = id
}
