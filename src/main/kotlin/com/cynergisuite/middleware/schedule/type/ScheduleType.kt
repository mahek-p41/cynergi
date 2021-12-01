package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.TypeDomainEntity

sealed interface ScheduleType: TypeDomainEntity<ScheduleType> {
   val id: Int
   val value: String
   val description: String
   val localizationCode: String

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

open class ScheduleTypeEntity(
   override val id: Int,
   override val value: String,
   override val description: String,
   override val localizationCode: String
) : ScheduleType {
   override fun equals(other: Any?): Boolean {
      return if (other != null && other is ScheduleType) {
         super.basicEquality(other)
      } else {
         false
      }
   }

   override fun hashCode(): Int = super.basicHashCode()
}

object Weekly : ScheduleTypeEntity(1, "WEEKLY", "Weekly", "schedule.weekly")
object Daily : ScheduleTypeEntity(1, "WEEKLY", "Weekly", "schedule.weekly") // this is a hack that needs to be cleaned up at some point, since the weekly job actually runs every day
object BeginningOfMonth : ScheduleTypeEntity(2, "BEGINNING_OF_MONTH", "Beginning of the month", "schedule.beginning.of.month")
object EndOfMonth : ScheduleTypeEntity(3, "END_OF_MONTH", "End of the month", "schedule.end.of.month")

typealias WEEKLY = Weekly
typealias BEGINNING_OF_MONTH = BeginningOfMonth
typealias END_OF_MONTH = EndOfMonth
