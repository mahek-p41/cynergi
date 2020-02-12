package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.infrastructure.AuthenticatedUserJwtClaimSetGenerator
import com.cynergisuite.middleware.authentication.user.infrastructure.AuthenticationRepository
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
   fun findUser(authentication: Authentication): User =
      authenticatedUserJwtClaimSetGenerator.reversePopulateWithUserDetails(authentication)

   fun fetchUserByAuthentication(number: Int, passCode: String, dataset: String, storeNumber: Int? = null): Maybe<User> =
      authenticationRepository.findUserByAuthentication(number, passCode, dataset, storeNumber)
}
