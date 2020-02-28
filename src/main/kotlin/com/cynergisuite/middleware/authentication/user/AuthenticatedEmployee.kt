package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.location.Location

data class AuthenticatedEmployee(
   val id: Long,
   val type: String, // sysz or eli
   val number: Int, // employee number
   val company: Company,
   val department: Department?,
   val location: Location?,
   val fallbackLocation: Location,
   val passCode: String,
   val cynergiSystemAdmin: Boolean
) : IdentifiableUser {

   constructor(user: AuthenticatedEmployee, passCodeOverride: String) :
      this(
         id = user.id,
         type = user.type,
         number = user.number,
         company = user.company,
         department = user.department,
         location = user.location,
         fallbackLocation = user.fallbackLocation,
         passCode = passCodeOverride,
         cynergiSystemAdmin = user.cynergiSystemAdmin
      )

   override fun myId(): Long = id
   override fun myEmployeeType(): String = type
   override fun myEmployeeNumber(): Int = number
}
