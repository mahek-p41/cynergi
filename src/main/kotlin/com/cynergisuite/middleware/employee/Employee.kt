package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.middleware.store.Store
import java.time.OffsetDateTime

data class Employee(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val loc: String,
   val number: Int,
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String,
   val store: Store,
   val active: Boolean = true
) : IdentifiableEntity {

   constructor(loc: String, number: Int, lastName: String, firstNameMi: String, passCode: String, store: Store, active: Boolean) :
      this(
         id = null,
         loc = loc,
         number = number,
         lastName = lastName,
         firstNameMi = firstNameMi,
         passCode = passCode,
         store = store,
         active = active
      )

   constructor(vo: EmployeeValueObject) :
      this(
         id = vo.id,
         loc = vo.loc!!,
         number = vo.number!!,
         lastName = vo.lastName!!,
         firstNameMi = vo.firstNameMi,
         passCode = vo.passCode!!,
         store = Store(vo.store!!),
         active = vo.active!!
      )

   override fun entityId(): Long? = id
   fun copyMe(): Employee = copy()
}
