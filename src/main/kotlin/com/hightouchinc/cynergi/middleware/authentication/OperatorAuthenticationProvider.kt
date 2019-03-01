package com.hightouchinc.cynergi.middleware.authentication

import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import org.reactivestreams.Publisher
import javax.inject.Singleton

@Singleton
class OperatorAuthenticationProvider : AuthenticationProvider {
   override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>): Publisher<AuthenticationResponse> {
         TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }
}
