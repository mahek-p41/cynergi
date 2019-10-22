package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.store.StoreEntity
import java.time.OffsetDateTime
import java.util.UUID

data class AuditScheduleEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val store: StoreEntity,
   val departmentAccess: DepartmentEntity,
   val schedule: ScheduleEntity
) : Entity<AuditScheduleEntity> {

   constructor(store: StoreEntity, departmentAccess: DepartmentEntity, schedule: ScheduleEntity) :
      this(
         id = null,
         store = store,
         departmentAccess = departmentAccess,
         schedule = schedule
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditScheduleEntity = copy()
}
