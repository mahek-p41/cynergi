package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.location.Location

data class EmployeeUser(
   val id: Long,
   val type: String, // sysz or eli
   val number: Int, // employee number
   val company: Company,
   val department: Department?,
   val location: Location?,
   val fallbackLocation: Location,
   val passCode: String,
   val cynergiSystemAdmin: Boolean
) : User {

   override fun myId(): Long = id
   override fun myCompany(): Company = company
   override fun myEmployeeType(): String = type
   override fun myLocation(): Location? = location
   override fun myEmployeeNumber(): Int = number
   override fun myDepartment(): Department? = department
}
