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
   val location: Location,
   val alternativeStoreIndicator: String,
   val alternativeArea: Int
): User, UserDetails(number.toString(), mutableListOf()) {

   constructor(employee: AuthenticatedEmployee) :
      this(
         id = employee.id,
         type = employee.type,
         number = employee.number,
         company = employee.company,
         department = employee.department,
         location = employee.location!!,
         alternativeStoreIndicator = employee.alternativeStoreIndicator,
         alternativeArea = employee.alternativeArea
      )

   constructor(employee: AuthenticatedEmployee, overrideStore: Location) :
      this(
         id = employee.id,
         type = employee.type,
         number = employee.number,
         company = employee.company,
         department = employee.department,
         location = overrideStore,
         alternativeStoreIndicator = employee.alternativeStoreIndicator,
         alternativeArea = employee.alternativeArea
      )

   override fun myId(): Long = id
   override fun myCompany(): Company = company
   override fun myEmployeeType(): String = type
   override fun myLocation(): Location = location
   override fun myEmployeeNumber(): Int = number
   override fun myDepartment(): Department? = department
   override fun myAlternativeStoreIndicator(): String = alternativeStoreIndicator
   override fun myAlternativeArea(): Int = alternativeArea
}
