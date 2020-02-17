package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreEntity.Companion.fromLocation

data class EmployeeEntity(
   val id: Long? = null,
   val type: String,
   val number: Int,
   val dataset: String,
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String,
   val store: StoreEntity?,
   val active: Boolean = true,
   val allowAutoStoreAssign: Boolean = false,
   val department: String? = null
) : User {

   constructor(vo: EmployeeValueObject) :
      this(
         id = vo.id,
         type = vo.type!!,
         number = vo.number!!,
         dataset = vo.dataset!!,
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
         dataset = user.myDataset(),
         lastName = user.myLastName(),
         firstNameMi = user.myFirstNameMi(),
         passCode = user.myPassCode(),
         store = fromLocation(user.myLocation()),
         active = true,
         allowAutoStoreAssign = user.doesAllowAutoStoreAssign()
      )

   override fun myId(): Long? = id
   override fun myFirstNameMi(): String? = firstNameMi
   override fun myLastName(): String = lastName
   override fun myPassCode(): String = passCode
   override fun myLocation(): Location? = store
   override fun myDepartment(): String? = department
   override fun amIActive(): Boolean = active
   override fun doesAllowAutoStoreAssign(): Boolean = allowAutoStoreAssign
   override fun myDataset(): String = dataset
   override fun myEmployeeType(): String = type
   override fun myStoreNumber(): Int? = store?.number
   override fun myEmployeeNumber(): Int = number

   fun copyMe(): EmployeeEntity = copy()
   fun displayName(): String = "$number - $lastName"
   fun getEmpName() : String = "$firstNameMi $lastName"

   companion object {

      @JvmStatic
      fun fromUser(user: User): EmployeeEntity {
         return if (user is EmployeeEntity) {
            user
         } else {
            EmployeeEntity(user)
         }
      }
   }
}
