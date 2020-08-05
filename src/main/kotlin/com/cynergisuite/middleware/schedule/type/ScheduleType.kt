package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.TypeDomainEntity

sealed class ScheduleType(
   open val id: Long,
   open val value: String,
   open val description: String,
   open val localizationCode: String
) : TypeDomainEntity<ScheduleType> {
   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun equals(other: Any?): Boolean {
      return if (other != null && other is ScheduleType) {
         super.basicEquality(other)
      } else {
         false
      }
   }

   override fun hashCode(): Int = super.basicHashCode()
}

data class ScheduleTypeEntity(
   override val id: Long,
   override val value: String,
   override val description: String,
   override val localizationCode: String
) : ScheduleType(id, value, description, localizationCode)

object Weekly : ScheduleType(1, "WEEKLY", "Weekly", "schedule.weekly")

typealias WEEKLY = Weekly
