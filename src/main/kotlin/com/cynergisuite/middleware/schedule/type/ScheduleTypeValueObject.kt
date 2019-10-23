package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase

@ValueObject
data class ScheduleTypeValueObject(
   var id: Long,
   var value: String,
   var description: String
) : ValueObjectBase<ScheduleTypeValueObject>() {

   constructor(entity: ScheduleTypeEntity, localizedDescription: String) :
      this(
         id = entity.id,
         value = entity.value,
         description = localizedDescription
      )

   override fun copyMe(): ScheduleTypeValueObject = copy()
   override fun valueObjectId(): Long? = id
}
