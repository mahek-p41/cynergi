package com.cynergisuite.middleware.authentication.infrastructure

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.web.router.RouteMatch
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

@Singleton
class CustomSecurityRule : SecurityRule{
   override fun check(
      request: HttpRequest<*>?,
      routeMatch: RouteMatch<*>?,
      authentication: Authentication?
   ): Mono<SecurityRuleResult> {
      if (authentication.roles.any{}) {
         return Mono.just(SecurityRuleResult.ALLOWED)
      }
      return Mono.just(SecurityRuleResult.REJECTED)
      return Mono.just(SecurityRuleResult.ALLOWED)
   }
}
