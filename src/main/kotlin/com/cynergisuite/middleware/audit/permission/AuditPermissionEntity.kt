package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.department.DepartmentEntity
import java.util.UUID

data class AuditPermissionEntity(
   val id: UUID? = null,
   val type: AuditPermissionType,
   val department: DepartmentEntity
) : Identifiable {
   override fun myId(): UUID? = id
}
