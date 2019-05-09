package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.Entity
import java.time.OffsetDateTime
import java.util.UUID

data class Employee(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: String,
   val passCode: String,
   val active: Boolean = true
) : Entity<Employee> {

   constructor(userId: String, passCode: String, active: Boolean) :
      this(
         id = null,
         number = userId,
         passCode = passCode,
         active = active
      )

   constructor(vo: EmployeeValueObject) :
      this(
         id = vo.id,
         number = vo.number!!,
         passCode = vo.passCode!!,
         active = vo.active!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Employee = copy()
}
