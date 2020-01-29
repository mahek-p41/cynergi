package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.department.DepartmentEntity
import java.time.OffsetDateTime
import java.util.UUID

data class AuditPermission(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val auditPermissionType: AuditPermissionType,
   val department: DepartmentEntity,
   val company: CompanyEntity
)
