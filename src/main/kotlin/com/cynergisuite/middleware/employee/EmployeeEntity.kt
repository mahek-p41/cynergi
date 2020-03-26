package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.store.Store

data class EmployeeEntity(
   val id: Long? = null,
   val type: String,
   val number: Int,
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String,
   val active: Boolean,
   val cynergiSystemAdmin: Boolean = false,
   val company: Company,
   val department: Department?,
   val store: Store?,
   val alternativeStoreIndicator: String,
   val alternativeArea: Int
) : Employee {

   constructor(vo: EmployeeValueObject, company: Company, department: Department?, store: Store?) :
      this(
         id = vo.id,
         type = vo.type!!,
         number = vo.number!!,
         lastName = vo.lastName!!,
         firstNameMi = vo.firstNameMi,
         passCode = vo.passCode!!,
         active = vo.active!!,
         company = company,
         department = department,
         store = store,
         alternativeStoreIndicator = vo.alternativeStoreIndicator!!,
         alternativeArea = vo.alternativeArea!!
      )

   fun getEmpName() : String = "$firstNameMi $lastName"
   fun displayName(): String = "$number - $lastName"

   override fun myId(): Long? = id
   override fun myNumber(): Int = number
   override fun copyMe(): EmployeeEntity = copy()
}
