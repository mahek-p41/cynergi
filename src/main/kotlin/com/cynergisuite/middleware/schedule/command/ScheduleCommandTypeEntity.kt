package com.cynergisuite.middleware.schedule.command

import com.cynergisuite.domain.TypeDomainEntity

sealed class ScheduleCommandType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<ScheduleCommandType> {
   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun equals(other: Any?): Boolean {
      return if (other != null && other is ScheduleCommandType) {
         super.basicEquality(other)
      } else {
         false
      }
   }

   override fun hashCode(): Int = super.basicHashCode()
}

class ScheduleCommandTypeEntity(
   id: Long,
   value: String,
   description: String,
   localizationCode: String
) : ScheduleCommandType(id, value, description, localizationCode)

object AuditSchedule: ScheduleCommandType(1, "AuditSchedule", "Scheduling audits for stores", "schedule.command.audit")

typealias AUDIT_SCHEDULE = AuditSchedule

