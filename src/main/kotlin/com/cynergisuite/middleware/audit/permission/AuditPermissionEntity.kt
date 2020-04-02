package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.department.DepartmentEntity

data class AuditPermissionEntity(
   val id: Long? = null,
   val type: AuditPermissionType,
   val department: DepartmentEntity
) : Identifiable {
   override fun myId(): Long? = id
}
