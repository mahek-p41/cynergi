package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.store.Store

data class AuthenticatedEmployee(
   val id: Long,
   val type: String, // sysz or eli
   val number: Int, // employee number
   val company: Company,
   val department: Department?,
   val location: Location?,
   val chosenLocation: Location?,
   val fallbackLocation: Location,
   val passCode: String,
   val cynergiSystemAdmin: Boolean,
   val alternativeStoreIndicator: String,
   val alternativeArea: Long
) : User {

   constructor(user: AuthenticatedEmployee, passCodeOverride: String) :
      this(
         id = user.id,
         type = user.type,
         number = user.number,
         company = user.company,
         department = user.department,
         location = user.location,
         chosenLocation = user.chosenLocation,
         fallbackLocation = user.fallbackLocation,
         passCode = passCodeOverride,
         cynergiSystemAdmin = user.cynergiSystemAdmin,
         alternativeStoreIndicator = user.alternativeStoreIndicator,
         alternativeArea = user.alternativeArea
      )

   constructor(employeeId: Long, employee: EmployeeEntity, store: Store) :
      this(
         id = employeeId,
         type = employee.type,
         number = employee.number,
         company = employee.company,
         department = employee.department,
         location = employee.store,
         chosenLocation = null, // since we are copying a row from the db for this, we don't have a chosenLocation
         fallbackLocation = store,
         passCode = employee.passCode,
         cynergiSystemAdmin = employee.cynergiSystemAdmin,
         alternativeStoreIndicator = employee.alternativeStoreIndicator,
         alternativeArea = employee.alternativeArea
      )

   override fun myId(): Long = id
   override fun myCompany(): Company = company
   override fun myDepartment(): Department? = department
   override fun myLocation(): Location = chosenLocation ?: location ?: fallbackLocation
   override fun myEmployeeType(): String = type
   override fun myEmployeeNumber(): Int = number
   override fun myAlternativeStoreIndicator(): String = alternativeStoreIndicator
   override fun myAlternativeArea(): Long = alternativeArea
   override fun isCynergiAdmin(): Boolean = cynergiSystemAdmin
}
