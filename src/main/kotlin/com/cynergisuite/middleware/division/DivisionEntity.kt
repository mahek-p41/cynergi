package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.SimpleEmployee
import java.time.OffsetDateTime
import java.util.*

data class DivisionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val company: Company,
   val number: Int,
   val name: String,
   val manager: Employee? = null,
   val description: String? = null
) : Entity<DivisionEntity> {
   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): DivisionEntity = copy()
}
