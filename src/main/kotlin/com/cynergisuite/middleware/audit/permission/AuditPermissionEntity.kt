package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.department.DepartmentEntity
import java.time.OffsetDateTime

data class AuditPermissionEntity(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val type: AuditPermissionType,
   val department: DepartmentEntity
) : Identifiable {
   override fun myId(): Long? = id
}
