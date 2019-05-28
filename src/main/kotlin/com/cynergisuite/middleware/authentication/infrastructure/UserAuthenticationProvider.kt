package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticatedUser
import com.cynergisuite.middleware.employee.EmployeeService
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
class UserAuthenticationProvider @Inject constructor(
   private val employeeService: EmployeeService
) : AuthenticationProvider {
   private val logger: Logger = LoggerFactory.getLogger(UserAuthenticationProvider::class.java)

   override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
      logger.debug("Authentication requested for user {}", authenticationRequest?.identity)

      return if (authenticationRequest != null && authenticationRequest.identity != null && authenticationRequest.secret != null) {
         employeeService.findUserByAuthentication((authenticationRequest.identity as String).toInt(), authenticationRequest.secret as String)
            .flatMapPublisher { Flowable.just(AuthenticatedUser(it)) }
      } else {
         Flowable.just<AuthenticationResponse>(AuthenticationFailed())
      }
   }
}
