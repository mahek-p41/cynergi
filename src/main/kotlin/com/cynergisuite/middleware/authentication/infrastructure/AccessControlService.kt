package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.AuthenticatedUser
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.localization.MessageCodes.System.ACCESS_DENIED
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.core.type.Argument
import io.micronaut.security.utils.SecurityService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessControlService @Inject constructor(
   private val employeeService: EmployeeService,
   private val securityService: SecurityService
) : MethodInterceptor<Any, Any> {

   override fun intercept(context: MethodInvocationContext<Any, Any>): Any {
      val authenticatedUser: AuthenticatedUser? = securityService.authentication.orElse(null) as AuthenticatedUser?
      val accessControl = context.annotationMetadata.getAnnotation(AccessControl::class.java)
      val asset = accessControl?.getValue(Argument.of(String::class.java, "asset"))?.orElse(null)
      val employee: EmployeeValueObject? = authenticatedUser?.let { employeeService.fetchById(it.id) }

      return if (securityService.isAuthenticated && asset != null && employee != null && employeeService.canEmployeeAccess(asset, employee)) {
         context.proceed()
      } else {
         throw AccessException(ACCESS_DENIED, securityService.username().orElse(null))
      }
   }
}
