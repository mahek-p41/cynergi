package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.TypeDomain

sealed class ScheduleType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,
) : TypeDomain() {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

open class ScheduleTypeEntity(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomain() {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

object Unknown : ScheduleType(0, "UNKNOWN", "Unknown", "schedule.unknown") // DO NOT INSERT THIS VALUE INTO THE DATABASE.  It is a placeholder for use as the fall through case
object Weekly : ScheduleType(1, "WEEKLY", "Weekly", "schedule.weekly")
object BeginningOfMonth : ScheduleType(2, "BEGINNING_OF_MONTH", "Beginning of the month", "schedule.beginning.of.month")
object EndOfMonth : ScheduleType(3, "END_OF_MONTH", "End of the month", "schedule.end.of.month")
object Daily : ScheduleType(4, "DAILY", "Daily", "schedule.daily")

fun ScheduleType.toEntity(): ScheduleTypeEntity =
   ScheduleTypeEntity(
      id = this.id,
      value = this.value,
      description = this.description,
      localizationCode = this.localizationCode,
   )

fun ScheduleTypeEntity.toType(): ScheduleType =
   when (this.id) {
      Weekly.id -> Weekly
      BeginningOfMonth.id -> BeginningOfMonth
      EndOfMonth.id -> EndOfMonth
      Daily.id -> Daily
      else -> Unknown // this is the fall through which will force the handling of this case, which will most likely be an error of some kind
   }
