package com.hightouchinc.cynergi.middleware.authentication

import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH
import io.micronaut.security.authentication.AuthenticationResponse
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationService @Inject constructor(
   private val client: PgPool
) {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticationService::class.java)

   fun authenticate(username: String, password: String): Single<AuthenticationResponse> {
      logger.debug("Attempting authentication for user {}", username)

      return client.rxPreparedQuery("SELECT level FROM employee WHERE username = $1 AND password = $2 LIMIT 1", Tuple.of(username, password)).map { rs ->
         val iterator = rs.iterator()

         if(iterator.hasNext()) {
            logger.trace("successfully authenticated user {}", username)

            val row = iterator.next()

            AuthenticatedUserDetails(userName = username, userRoles = emptySet(), level = row.getInteger("level"))
         } else {
            logger.trace("Unable to authenticate user {}", username)

            AuthenticationFailed(CREDENTIALS_DO_NOT_MATCH)
         }
      }
   }
}
