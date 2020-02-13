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
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String? = null,
   val active: Boolean,
   val cynergiSystemAdmin: Boolean = false,
   val company: Company,
   val department: Department?,
   val store: StoreEntity?
) : Identifiable {
   constructor(vo: EmployeeValueObject, company: Company, department: Department?, store: StoreEntity?) :
      this(
         id = vo.id,
         type = vo.type!!,
         number = vo.number!!,
         lastName = vo.lastName!!,
         firstNameMi = vo.firstNameMi,
         passCode = vo.passCode,
         active = vo.active!!,
         company = company,
         department = department,
         store = store
      )

   fun displayName(): String = "$number - $lastName"
   fun getEmpName() : String = "$firstNameMi $lastName"

   override fun myId(): Long? = id
}
