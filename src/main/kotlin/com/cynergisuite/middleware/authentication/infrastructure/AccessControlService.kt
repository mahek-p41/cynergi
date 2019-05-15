package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.localization.MessageCodes.System.ACCESS_DENIED
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.security.utils.SecurityService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessControlService @Inject constructor(
   private val securityService: SecurityService
) : MethodInterceptor<Any, Any> {

   override fun intercept(context: MethodInvocationContext<Any, Any>): Any {
      val securityLevel = context.annotationMetadata.getAnnotation(AccessControl::class.java)

      return if (securityService.isAuthenticated && securityLevel != null) {
         context.proceed()
      } else {
         throw AccessException(ACCESS_DENIED, securityService.username().orElse(null))
      }
   }
}
