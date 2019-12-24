package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.AuthenticationService
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.localization.AccessDenied
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.security.utils.SecurityService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles the AOP Around advice managed by attaching the AccessControl annotation to methods where a user's level can
 * be checked against the menu/modules when they are available.
 *
 * @param employeeService the service that will be used to load and then check an employee's access
 * @param securityService the Micronaut provided SecurityService that will be used to determine authentication details
 * for the employee who is attempting access to the endpoint
 */
@Singleton
class AccessControlService @Inject constructor(
   private val authenticationService: AuthenticationService,
   private val employeeService: EmployeeService,
   private val securityService: SecurityService
) : MethodInterceptor<Any, Any> {

   override fun intercept(context: MethodInvocationContext<Any, Any>): Any {
      val authenticatedUser = securityService.authentication.orElse(null)
      val accessControl = context.annotationMetadata.getAnnotation(AccessControl::class.java)
      val asset = accessControl?.values?.get("asset") as String?
      val user = authenticationService.findUser(authenticatedUser)

      return if (securityService.isAuthenticated && asset != null && employeeService.canEmployeeAccess(asset, user)) {
         context.proceed()
      } else {
         val username = securityService.username().orElse(null)

         throw AccessException(AccessDenied(username), username)
      }
   }
}
