package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.TypeDomainEntity
// TODO schedule need to figure out a way using a sealed class or sealed interface in such a way that we get some compile time checks when a new value is added to the object listing below
sealed interface ScheduleType : TypeDomainEntity<ScheduleType> {
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
object Daily : ScheduleTypeEntity(1, "WEEKLY", "Weekly", "schedule.weekly") // TODO schedule make this a real value in the database
object BeginningOfMonth : ScheduleTypeEntity(2, "BEGINNING_OF_MONTH", "Beginning of the month", "schedule.beginning.of.month")
object EndOfMonth : ScheduleTypeEntity(3, "END_OF_MONTH", "End of the month", "schedule.end.of.month")
// TODO schedule add additional values here as they are needed

typealias WEEKLY = Weekly
typealias BEGINNING_OF_MONTH = BeginningOfMonth
typealias END_OF_MONTH = EndOfMonth
