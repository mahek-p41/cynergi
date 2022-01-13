package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.location.Location
import io.micronaut.security.authentication.ServerAuthentication

data class AuthenticatedUser(
   val id: Long,
   val type: String, // sysz or eli
   val number: Int, // employee number
   val company: CompanyEntity,
   val department: Department?,
   val location: Location,
   val alternativeStoreIndicator: String,
   val alternativeArea: Long,
   val cynergiSystemAdmin: Boolean
) : User, ServerAuthentication(
   number.toString(),
   mutableListOf(), // TODO add roles here
   mutableMapOf<String, Any>(
      "id" to id.toString(),
      "tp" to type,
      "sub" to number.toString(),
      "ci" to company.id.toString(),
      "sn" to location.myNumber().toString()
   )
) {

   constructor(employee: AuthenticatedEmployee) :
      this(
         id = employee.id,
         type = employee.type,
         number = employee.number,
         company = employee.company,
         department = employee.department,
         location = employee.myLocation(),
         alternativeStoreIndicator = employee.alternativeStoreIndicator,
         alternativeArea = employee.alternativeArea,
         cynergiSystemAdmin = employee.cynergiSystemAdmin
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
         alternativeArea = employee.alternativeArea,
         cynergiSystemAdmin = employee.cynergiSystemAdmin
      )

   override fun myId(): Long = id
   override fun myCompany(): CompanyEntity = company
   override fun myEmployeeType(): String = type
   override fun myLocation(): Location = location
   override fun myEmployeeNumber(): Int = number
   override fun myDepartment(): Department? = department
   override fun myAlternativeStoreIndicator(): String = alternativeStoreIndicator
   override fun myAlternativeArea(): Long = alternativeArea
   override fun isCynergiAdmin(): Boolean = cynergiSystemAdmin
}
