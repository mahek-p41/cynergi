package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.CredentialsProvidedDidNotMatch
import com.cynergisuite.middleware.authentication.CredentialsRequireStore
import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.authentication.UserAuthenticated
import com.cynergisuite.middleware.authentication.UserAuthenticatedAsAdmin
import com.cynergisuite.middleware.authentication.UserAuthenticationStatus
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.location.Location
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class UserAuthenticationProvider @Inject constructor(
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(UserAuthenticationProvider::class.java)

   fun authenticate(authenticationRequest: LoginCredentials): UserAuthenticationStatus {
      logger.info("Authentication requested for user {}", authenticationRequest.username)

      val userNumber = authenticationRequest.username!!.toInt()
      val secret = authenticationRequest.password!!
      val storeNumber = authenticationRequest.storeNumber
      val dataset = authenticationRequest.dataset!!

      val employee = userService.fetchUserByAuthentication(userNumber, secret, dataset, storeNumber)

      return if (employee != null) {
         val employeeAssignedStore = employee.assignedLocation // this can be null which unless user is a cynergi admin you must have a store assigned
         val chosenStore = employee.chosenLocation // this is what the user chose as their store during login
         val fallbackStore = employee.fallbackLocation // use this if user is a cynergi admin and they didn't pick a store to log into

         if (employee.isCynergiAdmin()) {
            logger.info("Employee {} is cynergi admin, authentication successful", authenticationRequest.username)

            credentialsAssociatedWithAdmin(employee, fallbackStore)
         } else if (chosenStore == null) {
            if (storeNumber != null) {
               logger.warn("Employee {} did not provide matching credentials or invalid chosen store, not authenticated", authenticationRequest.username)

               credentialsProvidedDidNotMatch(userNumber)
            } else {
               logger.info("Employee {} required choosing a store number poorly and are assigned {}, not authenticated", userNumber, employeeAssignedStore)

               credentialsRequireStore(userNumber)
            }
         } else {
            // cases when chosenStore != null

            if (employeeAssignedStore == chosenStore || employee.alternativeStoreIndicator == "A") {
               logger.info("Employee {} has alternative store indicator set to A and chose store {}, authentication successful", employee, chosenStore)

               credentialsMatched(chosenStore, employee)
            } else {
               logger.info("Employee {} did not provide matching credentials or invalid chosen store, not authenticated", authenticationRequest.username)

               credentialsProvidedDidNotMatch(userNumber)
            }
         }
      } else {
         credentialsProvidedDidNotMatch(userNumber)
      }
   }

   private fun credentialsAssociatedWithAdmin(employee: AuthenticatedEmployee, fallbackStore: Location): UserAuthenticatedAsAdmin {
      logger.debug("Employee is system admin")

      return UserAuthenticatedAsAdmin(AuthenticatedUser(employee, employee.chosenLocation ?: fallbackStore))
   }

   private fun credentialsMatched(employeeAssignedStore: Location?, employee: AuthenticatedEmployee): UserAuthenticated {
      logger.debug("Employee chosen store matched assigned store, allowing access", employeeAssignedStore)

      return UserAuthenticated(AuthenticatedUser(employee))
   }

   private fun credentialsRequireStore(identity: Int): CredentialsRequireStore {
      logger.debug("Employee did not have store informing client of store requirement")

      return CredentialsRequireStore(identity)
   }

   private fun credentialsProvidedDidNotMatch(userId: Int): CredentialsProvidedDidNotMatch {
      logger.debug("Credentials provided did not match any known user/password/company/store combo")

      return CredentialsProvidedDidNotMatch(userId)
   }
}
