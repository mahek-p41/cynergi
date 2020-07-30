package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.localization.AccessDenied
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.ApplicationContext
import io.micronaut.security.utils.SecurityService
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles the AOP Around advice managed by attaching the AccessControl annotation to methods where a user's level can
 * be checked against the menu/modules when they are available.
 *
 * @param userService the AuthenticationService that handles interacting with the Micronaut Authentication
 * @param securityService the Micronaut provided SecurityService that will be used to determine authentication details
 * for the employee who is attempting access to the endpoint
 */
@Singleton
class AccessControlService @Inject constructor(
   private val applicationContext: ApplicationContext,
   private val userService: UserService,
   private val securityService: SecurityService
) : MethodInterceptor<Any, Any?> {
   private val logger: Logger = LoggerFactory.getLogger(AccessControlService::class.java)

   @Throws(AccessException::class)
   override fun intercept(context: MethodInvocationContext<Any, Any?>): Any? {
      val parameters = context.parameters
      val authenticatedUser: User = securityService.authentication.map { userService.findUser(it) }.orElseThrow { AccessException(AccessDenied(), securityService.username().orElse(null)) }
      val accessControl = context.annotationMetadata.getAnnotation(AccessControl::class.java)
      val asset: String = accessControl!!.stringValue()!!.orElse(EMPTY)
      val accessControlProviderClass = accessControl.classValue("accessControlProvider", AccessControlProvider::class.java)?.orElse(DefaultAccessControlProvider::class.java) ?: DefaultAccessControlProvider::class.java
      val accessControlProvider = applicationContext.getBean(accessControlProviderClass)

      logger.trace("Checking access of asset {} using {} of user {}", asset, accessControlProviderClass, authenticatedUser)

      return if (
         securityService.isAuthenticated
         && (authenticatedUser.isCynergiAdmin() || accessControlProvider.canUserAccess(authenticatedUser, asset, parameters))
      ) {
         context.proceed()
      } else {
         throw accessControlProvider.generateException(authenticatedUser, asset, parameters)
      }
   }
}
