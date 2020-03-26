package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.department.DepartmentEntity
import java.time.OffsetDateTime
import java.util.UUID

data class AuditPermissionEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val type: AuditPermissionType,
   val department: DepartmentEntity
) : Entity<AuditPermissionEntity> {
   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditPermissionEntity = copy()
}
