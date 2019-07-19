package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import io.micronaut.security.authentication.Authentication
import java.util.Objects
import javax.inject.Singleton

@Singleton
class AuthenticationService(
   private val employeeRepository: EmployeeRepository
) {

   fun findEmployee(authentication: Authentication?): EmployeeValueObject? {
      val employeeId: Long? = authentication?.attributes?.get("id").let { Objects.toString(it).toLong() }
      val employeeLoc: String? = authentication?.attributes?.get("loc").let { Objects.toString(it) }
      val storeNumber: Int? = authentication?.attributes?.get("storeNumber").let { Objects.toString(it).toInt() }

      return if (employeeId != null && employeeLoc != null && storeNumber != null) {
         employeeRepository.findOne(employeeId, employeeLoc, storeNumber)?.let { EmployeeValueObject(it) }
      } else {
         null
      }
   }
}
