package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticatedUser
import com.cynergisuite.middleware.authentication.AuthenticationResponseStoreRequired
import com.cynergisuite.middleware.authentication.StandardAuthenticatedUser
import com.cynergisuite.middleware.authentication.UsernamePasswordStoreCredentials
import com.cynergisuite.middleware.employee.EmployeeService
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.reactivex.Flowable.just
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAuthenticationProvider @Inject constructor(
   private val employeeService: EmployeeService
) : AuthenticationProvider {
   private val logger: Logger = LoggerFactory.getLogger(UserAuthenticationProvider::class.java)

   override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
      logger.debug("Authentication requested for user {}", authenticationRequest?.identity)

      val identity = (authenticationRequest?.identity as String?)?.toInt()
      val secret = authenticationRequest?.secret as String?
      val storeNumber = if (authenticationRequest is UsernamePasswordStoreCredentials) authenticationRequest.storeNumber else null

      return if (identity != null && secret != null) {
         employeeService
            .fetchUserByAuthentication(identity, secret, storeNumber)
            .flatMapPublisher { employee ->
               val employeeStore = employee.store

               if (employeeStore != null) { // if employee has store then proceed
                  just(StandardAuthenticatedUser(employee, employee.store))
               } else { // otherwise inform the client that a store is required for the provided user
                  just(AuthenticationResponseStoreRequired(identity))
               }
            }
      } else {
         just(AuthenticationFailed(CREDENTIALS_DO_NOT_MATCH))
      }
   }
}
