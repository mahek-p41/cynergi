package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.domain.Identifiable

data class ScheduleArgumentEntity(
   val id: Long? = null,
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
