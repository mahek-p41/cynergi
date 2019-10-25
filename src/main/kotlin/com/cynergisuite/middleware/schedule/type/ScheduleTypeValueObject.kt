package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@ValueObject
data class ScheduleTypeValueObject(

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   var value: String? = null,

   @field:NotNull
   var description: String? = null

) : ValueObjectBase<ScheduleTypeValueObject>() {

   constructor(entity: ScheduleTypeEntity, localizedDescription: String) :
      this(
         id = entity.id,
         value = entity.value,
         description = localizedDescription
      )

   override fun copyMe(): ScheduleTypeValueObject = copy()
   override fun myId(): Long? = id
}
