package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.TypeDomainEntity

sealed class ScheduleType(
   open val id: Int,
   open val value: String,
   open val description: String,
   open val localizationCode: String
) : TypeDomainEntity<ScheduleType> {
   override fun myId(): Int = id
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
   override val id: Int,
   override val value: String,
   override val description: String,
   override val localizationCode: String
) : ScheduleType(id, value, description, localizationCode)

object Weekly : ScheduleType(1, "WEEKLY", "Weekly", "schedule.weekly")
object BeginningOfMonth : ScheduleType(2, "BEGINNING_OF_MONTH", "Beginning of the month", "schedule.beginning.of.month")
object EndOfMonth : ScheduleType(3, "END_OF_MONTH", "End of the month", "schedule.end.of.month")

typealias WEEKLY = Weekly
typealias BEGINNING_OF_MONTH = BeginningOfMonth
typealias END_OF_MONTH = EndOfMonth
