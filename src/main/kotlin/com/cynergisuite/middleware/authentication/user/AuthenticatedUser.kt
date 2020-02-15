package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.location.Location
import io.micronaut.security.authentication.UserDetails

data class AuthenticatedUser(
   val id: Long,
   val type: String, // sysz or eli
   val number: Int, // employee number
   val company: Company,
   val department: Department?,
   val location: Location
): User, UserDetails(number.toString(), mutableListOf()) {

   constructor(employee: AuthenticatedEmployee) :
      this(
         id = employee.id, // this could cause a problem
         type = employee.type,
         number = employee.number,
         company = employee.company,
         department = employee.department,
         location = employee.location!!
      )

   constructor(employee: AuthenticatedEmployee, overrideStore: Location) :
      this(
         id = employee.id, // this could cause a problem
         type = employee.type,
         number = employee.number,
         company = employee.company,
         department = employee.department,
         location = overrideStore
      )

   override fun myId(): Long = id
   override fun myCompany(): Company = company
   override fun myEmployeeType(): String = type
   override fun myLocation(): Location = location
   override fun myEmployeeNumber(): Int = number
   override fun myDepartment(): Department? = department
}
