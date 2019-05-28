package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import io.micronaut.security.authentication.Authentication
import java.util.Objects
import javax.inject.Singleton

@Singleton
class AuthenticationService(
   private val employeeService: EmployeeService
) {

   fun findEmployee(authenticatedUser: Authentication?): EmployeeValueObject? {
      val employeeId: Long? = authenticatedUser?.attributes?.get("id").let { Objects.toString(it).toLong() }
      val employeeLoc: String? = authenticatedUser?.attributes?.get("loc").let { Objects.toString(it) }

      return if (employeeId != null && employeeLoc != null) employeeService.fetchById(employeeId, employeeLoc) else null
   }
}
