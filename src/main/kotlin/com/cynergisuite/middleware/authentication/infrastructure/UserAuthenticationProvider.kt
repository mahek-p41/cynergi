package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticationResponseStoreRequired
import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.location.Location
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.reactivex.Flowable
import io.reactivex.Flowable.just
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAuthenticationProvider @Inject constructor(
   private val userService: UserService
) : AuthenticationProvider {
   private val logger: Logger = LoggerFactory.getLogger(UserAuthenticationProvider::class.java)

   override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
      logger.debug("Authentication requested for user {}", authenticationRequest?.identity)

      val userNumber = (authenticationRequest?.identity as String?)?.toInt()
      val secret = authenticationRequest?.secret as String?
      val storeNumber = if (authenticationRequest is LoginCredentials) authenticationRequest.storeNumber else null
      val dataset = if (authenticationRequest is LoginCredentials) authenticationRequest.dataset else null

      return if (userNumber != null && secret != null && dataset != null) {
         userService
            .fetchUserByAuthentication(userNumber, secret, dataset, storeNumber)
            .flatMapPublisher { employee ->
               val employeeAssignedStore = employee.location // this can be null which unless user is a cynergi admin you must have a store assigned
               val chosenStore = employee.chosenLocation // this is what the user chose as their store during login
               val fallbackStore = employee.fallbackLocation // use this if user is a cynergi admin and they didn't pick a store to log into

               if (employee.cynergiSystemAdmin) {
                  credentialsAssociatedWithAdmin(employee, fallbackStore)
               } else if (employeeAssignedStore != null) {
                  if (employeeAssignedStore == chosenStore) {
                     credentialsMatched(employeeAssignedStore, employee)
                  } else if (storeNumber != null && chosenStore == null) {
                     credentialsProvidedDidNotMatch()
                  } else {
                     credentialsRequireStore(userNumber)
                  }
               } else if (chosenStore != null){
                  credentialsMatched(employeeAssignedStore, employee)
               } else {
                  credentialsProvidedDidNotMatch()
               }
            }
            .defaultIfEmpty(AuthenticationFailed(CREDENTIALS_DO_NOT_MATCH))
      } else {
         logger.debug("Employee {} was unable to be authenticated", userNumber)

         just(AuthenticationFailed(CREDENTIALS_DO_NOT_MATCH))
      }
   }

   private fun credentialsAssociatedWithAdmin(employee: AuthenticatedEmployee, fallbackStore: Location): Flowable<AuthenticatedUser> {
      logger.debug("Employee is system admin")

      return just(AuthenticatedUser(employee, employee.location ?: fallbackStore))
   }

   private fun credentialsRequireStore(identity: Int): Flowable<AuthenticationResponseStoreRequired> {
      logger.debug("Employee did not have store informing client of store requirement")

      return just(AuthenticationResponseStoreRequired(identity))
   }

   private fun credentialsMatched(employeeAssignedStore: Location?, employee: AuthenticatedEmployee): Flowable<AuthenticatedUser> {
      logger.debug("Employee chosen store matched assigned store, allowing access", employeeAssignedStore)

      return just(AuthenticatedUser(employee))
   }

   private fun credentialsProvidedDidNotMatch(): Flowable<AuthenticationFailed> {
      logger.debug("Credentials provided did not match any known user/password/company/store combo")

      return just(AuthenticationFailed(CREDENTIALS_DO_NOT_MATCH))
   }
}
