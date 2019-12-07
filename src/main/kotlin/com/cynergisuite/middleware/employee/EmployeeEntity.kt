package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.store.StoreEntity
import java.time.OffsetDateTime

data class EmployeeEntity(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val type: String,
   val number: Int,
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String,
   val store: StoreEntity?,
   val active: Boolean = true,
   val allowAutoStoreAssign: Boolean = false,
   val department: String? = null
) : Identifiable {

   constructor(type: String, number: Int, lastName: String, firstNameMi: String, passCode: String, store: StoreEntity, active: Boolean, allowAutoStoreAssign: Boolean, department: String? = null) :
      this(
         id = null,
         type = type,
         number = number,
         lastName = lastName,
         firstNameMi = firstNameMi,
         passCode = passCode,
         store = store,
         active = active,
         allowAutoStoreAssign = allowAutoStoreAssign,
         department = department
      )

   constructor(vo: EmployeeValueObject) :
      this(
         id = vo.id,
         type = vo.type!!,
         number = vo.number!!,
         lastName = vo.lastName!!,
         firstNameMi = vo.firstNameMi,
         passCode = vo.passCode!!,
         store = StoreEntity(vo.store!!),
         active = vo.active!!
      )

   override fun myId(): Long? = id
   fun copyMe(): EmployeeEntity = copy()
   fun displayName(): String = "$number - $lastName"
   fun getEmpName() : String = "$firstNameMi $lastName"
}
