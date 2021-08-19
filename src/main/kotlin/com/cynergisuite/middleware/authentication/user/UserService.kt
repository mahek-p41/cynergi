package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.infrastructure.AuthenticatedUserJwtClaimSetGenerator
import com.cynergisuite.middleware.authentication.user.infrastructure.AuthenticationRepository
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.security.authentication.Authentication
import io.reactivex.Maybe
import javax.inject.Singleton

@Singleton
class UserService(
   private val authenticationRepository: AuthenticationRepository,
   private val authenticatedUserJwtClaimSetGenerator: AuthenticatedUserJwtClaimSetGenerator
) {

   @Throws(NotFoundException::class, AccessException::class)
   fun fetchUser(authentication: Authentication): User =
      authenticatedUserJwtClaimSetGenerator.reversePopulateWithUserDetails(authentication)

   @Throws(NotFoundException::class, AccessException::class)
   fun fetchPermissions(department: Department): Set<String> =
      authenticationRepository.findPermissions(department)

   fun fetchUserByAuthentication(number: Int, passCode: String, dataset: String, storeNumber: Int? = null): Maybe<AuthenticatedEmployee> =
      if (storeNumber != null) {
         authenticationRepository.findUserByAuthenticationWithStore(number, passCode, dataset, storeNumber)
      } else {
         authenticationRepository.findUserByAuthentication(number, passCode, dataset)
      }

   fun fetchAllPermissions(): Set<String> =
      authenticationRepository.findAllPermissions()
}
