package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.Employee
import io.micronaut.security.authentication.UserDetails

class AuthenticatedUser(
   username: String
) : UserDetails(username, mutableListOf()) {
   constructor(employee: Employee) :
      this (
         username = employee.number
      )
}
