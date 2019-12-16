package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.localization.NotLoggedIn
import io.micronaut.security.authentication.Authentication
import javax.inject.Singleton

@Singleton
class AuthenticationService(
   private val employeeRepository: EmployeeRepository
) {

   @Throws(NotFoundException::class, AccessException::class)
   fun findUser(authentication: Authentication): User =
      employeeRepository
         .findOne(StandardAuthenticatedUser(authentication))
         ?: throw AccessException(NotLoggedIn(), authentication = authentication)
}
