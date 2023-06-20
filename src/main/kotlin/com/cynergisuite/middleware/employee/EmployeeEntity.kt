package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.store.Store

data class EmployeeEntity(
   val id: Long? = null,
   val type: String,
   val number: Int,
   val lastName: String,
   val firstNameMi: String?,
   val passCode: String,
   val active: Boolean,
  // val cynergiSystemAdmin: Boolean = false,
   val company: CompanyEntity,
   val department: DepartmentEntity?,
   val store: Store? = null,
   val alternativeStoreIndicator: String,
   val alternativeArea: Long,
   val securityGroup: SecurityGroup
) : Employee {

   constructor(vo: EmployeeValueObject, company: CompanyEntity, department: DepartmentEntity?, store: Store?, securityGroup: SecurityGroup) :
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
         alternativeArea = vo.alternativeArea!!,
         securityGroup = securityGroup
      )

   fun getEmpName(): String = "$firstNameMi $lastName"
   fun displayName(): String = "$number - $lastName"

   override fun myId(): Long? = id
   override fun myNumber(): Int = number
   override fun copyMe(): EmployeeEntity = copy()
   fun copyMeWithDifferentPassCode(passCode: String): EmployeeEntity = copy(passCode = passCode)
}
