package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.localization.AccessDenied
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.ApplicationContext
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Handles the AOP Around advice managed by attaching the AccessControl annotation to methods where a user's level can
 * be checked against the menu/modules when they are available.
 *
 * @param userService the AuthenticationService that handles interacting with the Micronaut Authentication
 * @param securityService the Micronaut provided SecurityService that will be used to determine authentication details
 * for the employee who is attempting access to the endpoint
 */
@Singleton
@InterceptorBean(AreaControl::class)
class AreaControlService @Inject constructor(
   private val applicationContext: ApplicationContext,
   private val userService: UserService,
   private val securityService: SecurityService
) : MethodInterceptor<Any, Any?> {
   private val logger: Logger = LoggerFactory.getLogger(AreaControlService::class.java)

   @Throws(AccessException::class)
   override fun intercept(context: MethodInvocationContext<Any, Any?>): Any? {
      val parameters = context.parameters
      val authenticatedUser: User = securityService.authentication.map { userService.fetchUser(it) }.orElseThrow { AccessException(AccessDenied(), securityService.username().orElse(null)) }
      val accessControl = context.annotationMetadata.getAnnotation(AreaControl::class.java)
      val asset: Array<String> =  accessControl.values.values.flatMap {when (it) {
         is Array<*> -> it.mapNotNull { element -> element.toString() }
         else -> listOf(it.toString())
      }}.toTypedArray()
      val areaControlProviderClass = AreaControlProvider::class.java
      val areaControlProvider = applicationContext.getBean(areaControlProviderClass)

      logger.trace("Checking access of asset {} using {} of user {}", asset, areaControlProviderClass, authenticatedUser)

      return if (
         securityService.isAuthenticated &&
         (authenticatedUser.isCynergiAdmin() || areaControlProvider.canUserAccess(authenticatedUser, asset, parameters))
      ) {
         context.proceed()
      } else {
         throw areaControlProvider.generateException(authenticatedUser, asset, parameters)
      }
   }

   val ORDER = SecuredAnnotationRule.ORDER - 10

   override fun getOrder(): Int {
      return ORDER
   }
}
