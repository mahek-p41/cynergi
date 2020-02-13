package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.store.StoreEntity
import java.util.UUID

data class EmployeeEntity(
   val id: Long? = null,
   val type: String,
   val number: Int,
   val company: Company,
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String,
   val store: StoreEntity?,
   val active: Boolean = true,
   val allowAutoStoreAssign: Boolean = false,
   val department: Department? = null
) : Identifiable {

   constructor(vo: EmployeeValueObject, company: Company) :
      this(
         id = vo.id,
         type = vo.type!!,
         number = vo.number!!,
         company = company,
         lastName = vo.lastName!!,
         firstNameMi = vo.firstNameMi,
         passCode = vo.passCode!!,
         store = vo.store?.let { StoreEntity(it, company) },
         active = vo.active!!,
         allowAutoStoreAssign = vo.allowAutoStoreAssign!!
      )

   override fun myId(): Long? = id
   fun displayName(): String = "$number - $lastName"
   fun getEmpName() : String = "$firstNameMi $lastName"
}
