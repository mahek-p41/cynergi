package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.middleware.schedule.ScheduleCreateDataTransferObject

@DataTransferObject
data class AuditScheduleCreateDataTransferObject(
   var store: SimpleIdentifiableValueObject,
   var departmentAccess: SimpleIdentifiableValueObject,
   var schedule: ScheduleCreateDataTransferObject
) {
}
