package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.Employee
import io.micronaut.security.authentication.UserDetails

class AuthenticatedUser(
   val id: Long,
   username: String
) : UserDetails(username, mutableListOf()) {
   constructor(employee: Employee) :
      this (
         id = employee.id!!,
         username = employee.number.toString()
      )
}
