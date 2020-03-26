package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.Identifiable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class ScheduleTypeValueObject(

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   var value: String? = null,

   @field:NotNull
   var description: String? = null

) : Identifiable {

   constructor(entity: ScheduleType, localizedDescription: String) :
      this(
         id = entity.id,
         value = entity.value,
         description = localizedDescription
      )

   override fun myId(): Long? = id
}
