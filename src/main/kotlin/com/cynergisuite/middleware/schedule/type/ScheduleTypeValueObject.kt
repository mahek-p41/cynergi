package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.ValueObject

@ValueObject
data class ScheduleTypeValueObject(
   val id: Long,
   val value: String,
   val description: String
) {
   constructor(entity: ScheduleTypeEntity, localizedDescription: String) :
      this(
         id = entity.id,
         value = entity.value,
         description = localizedDescription
      )
}
