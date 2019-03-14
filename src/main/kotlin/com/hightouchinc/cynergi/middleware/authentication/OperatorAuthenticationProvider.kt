package com.hightouchinc.cynergi.middleware.authentication

import io.micronaut.context.annotation.Requires
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(env = ["local", "prod", "demo"])
class OperatorAuthenticationProvider @Inject constructor(
   private val authenticationService: AuthenticationService
) : AuthenticationProvider {
   private val logger: Logger = LoggerFactory.getLogger(OperatorAuthenticationProvider::class.java)

   override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
      logger.debug("Authentication requested for user {}", authenticationRequest?.identity)

      return if (authenticationRequest != null && authenticationRequest.identity != null && authenticationRequest.secret != null) {

         authenticationService.authenticate(username = authenticationRequest.identity as String, password = authenticationRequest.secret as String).toFlowable()
      } else {
         Flowable.just<AuthenticationResponse>(AuthenticationFailed())
      }
   }
}
