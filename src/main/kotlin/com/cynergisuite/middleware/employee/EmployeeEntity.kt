package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity

data class EmployeeEntity(
   val id: Long? = null,
   val type: String,
   val number: Int,
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String,
   val store: StoreEntity?,
   val active: Boolean = true,
   val allowAutoStoreAssign: Boolean = false,
   val department: String? = null
) : User {

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
         store = vo.store?.let { StoreEntity(it) },
         active = vo.active!!,
         allowAutoStoreAssign = vo.allowAutoStoreAssign!!
      )

   constructor(user: User) :
      this(
         id = user.myId(),
         type = user.myEmployeeType(),
         number = user.myEmployeeNumber(),
         lastName = user.myLastName(),
         firstNameMi = user.myFirstNameMi(),
         passCode = user.myPassCode(),
         store = StoreEntity.from(user.myStore()),
         active = true,
         allowAutoStoreAssign = user.doesAllowAutoStoreAssign()
      )

   override fun myId(): Long? = id
   override fun myFirstNameMi(): String? = firstNameMi
   override fun myLastName(): String = lastName
   override fun myPassCode(): String = passCode
   override fun myStore(): Store? = store
   override fun myDepartment(): String? = department
   override fun amIActive(): Boolean = active
   override fun doesAllowAutoStoreAssign(): Boolean = allowAutoStoreAssign
   override fun myEmployeeType(): String = type
   override fun myStoreNumber(): Int? = store?.number
   override fun myEmployeeNumber(): Int = number

   fun copyMe(): EmployeeEntity = copy()
   fun displayName(): String = "$number - $lastName"
   fun getEmpName() : String = "$firstNameMi $lastName"

   companion object {

      @JvmStatic
      fun from(user: User): EmployeeEntity {
         return if (user is EmployeeEntity) {
            user
         } else {
            EmployeeEntity(user)
         }
      }
   }
}
