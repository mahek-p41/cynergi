package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.domain.ValueObject

@ValueObject
data class ScheduleArgumentValueObject(
   val id: Long? = null,
   val value: String,
   val description: String
) {
   constructor(entity: ScheduleArgumentEntity) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )
}
