package com.hightouchinc.cynergi.middleware.authentication

import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OperatorAuthenticationProvider @Inject constructor(
   private val authenticationService: AuthenticationService
) : AuthenticationProvider {
   private val logger: Logger = LoggerFactory.getLogger(OperatorAuthenticationProvider::class.java)

   override fun authenticate(authenticationRequest: AuthenticationRequest<in String, in String>): Publisher<AuthenticationResponse> {
      logger.debug("Authentication requested {}/{}", authenticationRequest.identity, authenticationRequest.secret)

      return authenticationService.authenticate(username = authenticationRequest.identity as String, password = authenticationRequest.secret as String).toFlowable()
   }
}
