package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.extensions.toUuid
import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.user.infrastructure.AuthenticationRepository
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.security.authentication.Authentication
import jakarta.inject.Singleton
import java.util.Objects

@Singleton
class UserService(
   private val authenticationRepository: AuthenticationRepository,
) {

   @Throws(NotFoundException::class, AccessException::class)
   fun fetchUser(authentication: Authentication): User {
      val employeeId = authentication.attributes["id"]?.let { Objects.toString(it).toLong() } ?: throw Exception("Unable to find employee ID")
      val employeeType = authentication.attributes["tp"]?.let { Objects.toString(it) } ?: throw Exception("Unable to find employee type")
      val employeeNumber = authentication.attributes["sub"]?.let { Objects.toString(it).toInt() } ?: throw Exception("Unable to find employee number")
      val companyId = authentication.attributes["ci"]?.let { Objects.toString(it).toUuid() } ?: throw Exception("Unable to find company ID")
      val storeNumber = authentication.attributes["sn"]?.let { Objects.toString(it).toInt() } ?: throw Exception("Unable to find store number")

      return authenticationRepository.findUser(employeeId, employeeType, employeeNumber, companyId, storeNumber) // this should be cached so the lookup should only be required once per user login
   }

   @Throws(NotFoundException::class, AccessException::class)
   fun fetchPermissions(department: Department): Set<String> =
      authenticationRepository.findPermissions(department)

   fun fetchUserByAuthentication(number: Int, passCode: String, dataset: String, storeNumber: Int? = null): AuthenticatedEmployee? =
      if (storeNumber != null) {
         authenticationRepository.findUserByAuthenticationWithStore(number, passCode, dataset, storeNumber)
      } else {
         authenticationRepository.findUserByAuthentication(number, passCode, dataset)
      }

   fun fetchAllPermissions(): Set<String> =
      authenticationRepository.findAllPermissions()
}
