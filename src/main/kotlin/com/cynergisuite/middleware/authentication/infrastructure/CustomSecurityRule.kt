package com.cynergisuite.middleware.authentication.infrastructure

import io.micronaut.http.HttpRequest
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.web.router.RouteMatch
import jakarta.inject.Singleton
import reactor.core.publisher.Mono


@Singleton
class CustomSecurityRule : SecurityRule{
   override fun check(
      request: HttpRequest<*>?,
      routeMatch: RouteMatch<*>?,
      authentication: Authentication?
   ): Mono<SecurityRuleResult> {
      val securedAnnotation = routeMatch?.annotationMetadata?.getAnnotation(Secured::class.java)
      val customRoles = securedAnnotation?.stringValues("value")

      if (authentication == null || customRoles.isNullOrEmpty() || customRoles.contains("isAuthenticated()") || customRoles.contains("isAnonymous()") ) {
         return Mono.just(SecurityRuleResult.UNKNOWN)
      }

      if( authentication!!.roles.any { it in customRoles } ) {
         return Mono.just(SecurityRuleResult.ALLOWED)
      }
      return Mono.just(SecurityRuleResult.UNKNOWN)
   }

   val ORDER = SecuredAnnotationRule.ORDER - 1

   override fun getOrder(): Int {
      return ORDER
   }
}
