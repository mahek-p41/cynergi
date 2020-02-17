package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.notification.NotificationValueObject
import com.cynergisuite.middleware.schedule.ScheduleResult

data class AuditScheduleResult(
   val audits: List<AuditValueObject>,
   val notifications: List<NotificationValueObject>
   ) : ScheduleResult {
   override fun scheduleName(): String = "AuditSchedule"
}
