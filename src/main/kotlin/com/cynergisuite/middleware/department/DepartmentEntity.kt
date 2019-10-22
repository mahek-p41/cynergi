package com.cynergisuite.middleware.department

import com.cynergisuite.domain.IdentifiableEntity
import java.time.OffsetDateTime

data class DepartmentEntity(
   val id: Long,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val code: String,
   val description: String,
   val securityProfile: Int,
   val defaultMenu: String
) : IdentifiableEntity {
   override fun entityId(): Long? = id
}
