package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.AuthenticationService
import com.cynergisuite.middleware.localization.AccessDenied
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.utils.SecurityService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles the AOP Around advice managed by attaching the AccessControl annotation to methods where a user's level can
 * be checked against the menu/modules when they are available.
 *
 * @param authenticationService the AuthenticationService that handles interacting with the Micronaut Authentication
 * @param securityService the Micronaut provided SecurityService that will be used to determine authentication details
 * for the employee who is attempting access to the endpoint
 */
@Singleton
class AccessControlService @Inject constructor(
   private val authenticationService: AuthenticationService,
   private val securityService: SecurityService
) : MethodInterceptor<Any, Any> {

   override fun intercept(context: MethodInvocationContext<Any, Any>): Any {
      val authenticatedUser: Authentication = securityService.authentication.orElseThrow { handleAccessDenied() }
      val accessControl = context.annotationMetadata.getAnnotation(AccessControl::class.java)
      val asset: String? = accessControl?.stringValue()?.orElse(null)

      return if (securityService.isAuthenticated && asset != null && authenticationService.canUserAccess(authenticatedUser, asset)) {
         context.proceed()
      } else {
         throw handleAccessDenied()
      }
   }

   @Throws(AccessException::class)
   private fun handleAccessDenied(): AccessException { // this method handles in a reusable way what to do if a user doesn't have access
      val username = securityService.username().orElse(null)

      return AccessException(AccessDenied(), username)
   }
}
