package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.localization.NotLoggedIn
import io.micronaut.security.authentication.Authentication
import java.util.Objects
import javax.inject.Singleton

@Singleton
class AuthenticationService(
   private val employeeRepository: EmployeeRepository
) {

   @Throws(NotFoundException::class, AccessException::class)
   fun findEmployee(authentication: Authentication?): EmployeeValueObject {
      val user = if (authentication  != null) {
         AuthenticatedUser(authentication)
      } else {
         throw AccessException(NotLoggedIn(), authentication = authentication)
      }

      return employeeRepository.findOne(user)
         ?.let { EmployeeValueObject(it) }
         ?: throw NotFoundException("employee")
   }
}
