package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.store.StoreEntity

data class EmployeeUser(
   val id: Long,
   val type: String,
   val number: Int,
   val company: Company,
   val department: Department?,
   val location: Location,
   val passCode: String
) : User {
   constructor(employeeEntity: EmployeeEntity, location: StoreEntity):
      this(
         id = employeeEntity.id!!,
         type = employeeEntity.type,
         number = employeeEntity.number,
         company = employeeEntity.company,
         department = employeeEntity.department,
         location = location,
         passCode = employeeEntity.passCode!!
      )
   override fun myId(): Long? = id
   override fun myEmployeeType(): String = type
   override fun myEmployeeNumber(): Int = number
   override fun myCompany(): Company = company
   override fun myDepartment(): Department? = department
   override fun myLocation(): Location = location
}
