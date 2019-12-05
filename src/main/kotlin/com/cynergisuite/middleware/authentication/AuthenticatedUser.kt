package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.UserDetails
import java.util.Objects

class AuthenticatedUser(
   val id: Long,
   val loc: String,
   val storeNumber: Int,
   val employeeNumber: Int
) : UserDetails(employeeNumber.toString(), mutableListOf()) {
   constructor(employee: EmployeeEntity, storeNumber: Int) :
      this (
         id = employee.id!!,
         loc = employee.loc,
         storeNumber = storeNumber,
         employeeNumber = employee.number
      )

   constructor(employee: EmployeeEntity, overrideStore: StoreEntity) :
      this (
         id = employee.id!!,
         loc = employee.loc,
         storeNumber = overrideStore.number,
         employeeNumber = employee.number
      )

   constructor(authentication: Authentication) :
      this(
         id = authentication.attributes.get("id").let { Objects.toString(it).toLong() },
         loc = authentication.attributes.get("loc").let { Objects.toString(it) },
         storeNumber = authentication.attributes.get("stNum").let { Objects.toString(it).toInt() },
         employeeNumber = authentication.attributes.get("sub").let { Objects.toString(it).toInt() }
      )
}
