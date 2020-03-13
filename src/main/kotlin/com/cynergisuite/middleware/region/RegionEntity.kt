package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.Employee
import java.time.OffsetDateTime
import java.util.UUID

data class RegionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val division: DivisionEntity,
   val number: Int,
   val name: String,
   val manager: Employee? = null,
   val description: String? = null
) : Entity<RegionEntity> {
   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): RegionEntity = copy()
}
