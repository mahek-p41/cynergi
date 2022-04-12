package com.cynergisuite.middleware.schedule.command

import com.cynergisuite.domain.TypeDomain

sealed class ScheduleCommandType(
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

open class ScheduleCommandTypeEntity(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomain() {
   constructor(scheduleCommandType: ScheduleCommandType) :
      this(
         id = scheduleCommandType.id,
         value = scheduleCommandType.value,
         description = scheduleCommandType.description,
         localizationCode = scheduleCommandType.localizationCode
      )

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

object Unknown : ScheduleCommandType(0, "UNKNOWN", "Unknown", "schedule.command.unknown") // DO NOT INSERT THIS VALUE INTO THE DATABASE.  It is a placeholder for use as the fall through case
object AuditSchedule : ScheduleCommandType(1, "AuditSchedule", "Scheduling audits for stores", "schedule.command.audit")
object DarwillInactiveCustomer : ScheduleCommandType(2, "DarwillInactiveCustomer", "Darwill Inactive Customers", "darwill.inactive.customers")
object DarwillActiveCustomer : ScheduleCommandType(3, "DarwillActiveCustomer", "Darwill Active Customers", "darwill.active.customers")
object DarwillBirthday : ScheduleCommandType(4, "DarwillBirthday", "Darwill Birthday", "darwill.birthday")
object DarwillCollection : ScheduleCommandType(5, "DarwillCollection", "Darwill Collection", "darwill.collection")
object DarwillLastWeeksDelivery : ScheduleCommandType(6, "DarwillLastWeeksDelivery", "Darwill Last Weeks Deliveries", "darwill.last.weeks.deliveries")
object DarwillLastWeeksPayout : ScheduleCommandType(7, "DarwillLastWeeksPayout", "Darwill Last Weeks Payouts", "darwill.last.weeks.payouts")

fun ScheduleCommandType.toEntity(): ScheduleCommandTypeEntity =
   ScheduleCommandTypeEntity(
      id = this.id,
      value = this.value,
      description = this.description,
      localizationCode = this.localizationCode,
   )

fun ScheduleCommandTypeEntity.toType(): ScheduleCommandType =
   when (this.id) {
      AuditSchedule.id -> AuditSchedule
      DarwillInactiveCustomer.id -> DarwillInactiveCustomer
      DarwillActiveCustomer.id -> DarwillActiveCustomer
      DarwillBirthday.id -> DarwillBirthday
      DarwillCollection.id -> DarwillCollection
      DarwillLastWeeksDelivery.id -> DarwillLastWeeksDelivery
      DarwillLastWeeksPayout.id -> DarwillLastWeeksPayout
      else -> Unknown // this is the fall through which will force the handling of this case, which will most likely be an error of some kind
   }
