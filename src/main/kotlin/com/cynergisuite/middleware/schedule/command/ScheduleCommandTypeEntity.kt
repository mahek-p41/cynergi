package com.cynergisuite.middleware.schedule.command

import com.cynergisuite.domain.TypeDomainEntity

data class ScheduleCommandTypeEntity(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<ScheduleCommandTypeEntity> {
   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun equals(other: Any?): Boolean {
      return if (other != null && other is ScheduleCommandTypeEntity) {
         super.basicEquality(other)
      } else {
         false
      }
   }

   override fun hashCode(): Int = super.basicHashCode()
}
