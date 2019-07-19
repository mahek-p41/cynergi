package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.store.Store
import io.micronaut.security.authentication.UserDetails

class AuthenticatedUser(
   val id: Long,
   val loc: String,
   val storeNumber: Int,
   username: String
) : UserDetails(username, mutableListOf()) {
   constructor(employee: Employee) :
      this (
         id = employee.id!!,
         loc = employee.loc,
         storeNumber = employee.store.number,
         username = employee.number.toString()
      )

   constructor(employee: Employee, overrideStore: Store) :
      this (
         id = employee.id!!,
         loc = employee.loc,
         storeNumber = overrideStore.number,
         username = employee.number.toString()
      )
}
