package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.Employee
import io.micronaut.security.authentication.UserDetails

class AuthenticatedUser(
   val id: Long,
   val loc: String,
   username: String
) : UserDetails(username, mutableListOf()) {
   constructor(employee: Employee) :
      this (
         id = employee.id!!,
         loc = employee.loc,
         username = employee.number.toString()
      )
}
