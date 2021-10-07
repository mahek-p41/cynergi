package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.notification.NotificationValueObject
import com.cynergisuite.middleware.schedule.JobResult

data class AuditScheduleResult(
   val audits: List<AuditValueObject>,
   val notifications: List<NotificationValueObject>
) : JobResult {
   override fun scheduleName(): String = "AuditSchedule"
}
