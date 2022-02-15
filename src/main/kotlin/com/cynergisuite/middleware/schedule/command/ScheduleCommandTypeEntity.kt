package com.cynergisuite.middleware.schedule.command

import com.cynergisuite.domain.TypeDomainEntity

sealed interface ScheduleCommandType : TypeDomainEntity<ScheduleCommandType> {
   val id: Int
   val value: String
   val description: String
   val localizationCode: String

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

open class ScheduleCommandTypeEntity(
   override val id: Int,
   override val value: String,
   override val description: String,
   override val localizationCode: String
) : ScheduleCommandType {
   constructor(scheduleCommandType: ScheduleCommandType) :
      this(
         id = scheduleCommandType.id,
         value = scheduleCommandType.value,
         description = scheduleCommandType.description,
         localizationCode = scheduleCommandType.localizationCode
      )

   override fun equals(other: Any?): Boolean {
      return if (other != null && other is ScheduleCommandType) {
         super.basicEquality(other)
      } else {
         false
      }
   }

   override fun hashCode(): Int = super.basicHashCode()
}

object AuditSchedule : ScheduleCommandTypeEntity(1, "AuditSchedule", "Scheduling audits for stores", "schedule.command.audit")
object DarwillInactiveCustomer : ScheduleCommandTypeEntity(2, "DarwillInactiveCustomer", "Darwill Inactive Customers", "darwill.inactive.customers")
object DarwillActiveCustomer : ScheduleCommandTypeEntity(3, "DarwillActiveCustomer", "Darwill Active Customers", "darwill.active.customers")
object DarwillBirthday : ScheduleCommandTypeEntity(4, "DarwillBirthday", "Darwill Birthday", "darwill.birthday")
object DarwillCollection : ScheduleCommandTypeEntity(5, "DarwillCollection", "Darwill Collection", "darwill.collection")
object DarwillLastWeeksDelivery : ScheduleCommandTypeEntity(6, "DarwillLastWeeksDelivery", "Darwill Last Weeks Deliveries", "darwill.last.weeks.deliveries")
object DarwillLastWeeksPayout : ScheduleCommandTypeEntity(7, "DarwillLastWeeksPayout", "Darwill Last Weeks Payouts", "darwill.last.weeks.payouts")

typealias AUDIT_SCHEDULE = AuditSchedule
typealias DARWILL_INACTIVE_CUSTOMER = DarwillInactiveCustomer
typealias DARWILL_ACTIVE_CUSTOMER = DarwillActiveCustomer
typealias DARWILL_BIRTHDAY = DarwillBirthday
typealias DARWILL_COLLECTION = DarwillCollection
typealias DARWILL_LAST_WEEK_DELIVERY = DarwillLastWeeksDelivery
typealias DARWILL_LAST_WEEK_PAYOUT = DarwillLastWeeksPayout
