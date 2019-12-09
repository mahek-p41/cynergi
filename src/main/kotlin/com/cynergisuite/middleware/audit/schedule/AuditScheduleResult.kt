package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.notification.Notification
import com.cynergisuite.middleware.notification.NotificationValueObject
import com.cynergisuite.middleware.schedule.ScheduleResult

data class AuditScheduleResult(
   val notifications: List<NotificationValueObject>,
   val audits: List<AuditValueObject>
) : ScheduleResult {
   override fun scheduleName(): String = "AuditSchedule"
}
