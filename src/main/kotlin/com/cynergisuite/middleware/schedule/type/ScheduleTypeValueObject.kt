package com.cynergisuite.middleware.schedule.type

import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class ScheduleTypeValueObject(

   @field:Positive
   var id: Int? = null,

   @field:NotNull
   var value: String? = null,

   @field:NotNull
   var description: String? = null

) {

   constructor(entity: ScheduleTypeEntity, localizedDescription: String) :
      this(
         id = entity.id,
         value = entity.value,
         description = localizedDescription,
      )

   constructor(type: ScheduleType, localizedDescription: String) :
      this(
         id = type.id,
         value = type.value,
         description = localizedDescription
      )

   fun myId(): Int? = id
}
