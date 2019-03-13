package com.hightouchinc.cynergi.middleware.authentication

import com.nimbusds.jwt.JWTClaimsSet
import io.micronaut.context.annotation.Requires
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.token.config.TokenConfiguration
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(env = ["local", "prod"])
class AuthenticationService @Inject constructor(
   private val client: PgPool,
   private val tokenConfiguration: TokenConfiguration
) {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticationService::class.java)

   fun authenticate(username: String, password: String): Single<AuthenticationResponse> {
      logger.debug("Attempting authentication for user {}", username)

      return client.rxPreparedQuery("""
         SELECT
            e.id AS user_id,
            d.level AS level,
            c.id AS company_id
         FROM employee e
              JOIN department d
                ON e.department_id = d.id
              JOIN company c
                ON d.company_id = c.id
         WHERE user_id = $1
               AND password = $2
         LIMIT 1
         """.trimIndent(), Tuple.of(username, password)).map { rs ->
         val iterator = rs.iterator()

         if(iterator.hasNext()) {
            logger.trace("successfully authenticated user {}", username)

            val row = iterator.next()

            AuthenticatedCynergiUser(
               username = username,
               level = row.getInteger("level"),
               companyId = row.getLong("company_id"),
               roles = mutableSetOf("ROLE_USER"), // TODO need to figure out something better than just hard coding everyone as a user, can the roles be defined by the modules or areas
               userId = row.getLong("user_id")
            )
         } else {
            logger.trace("Unable to authenticate user {}", username)

            AuthenticationFailed(CREDENTIALS_DO_NOT_MATCH)
         }
      }
   }

   @Suppress("UNCHECKED_CAST")
   fun decodeUserDetails(authentication: Authentication): AuthenticatedCynergiUser {
      return AuthenticatedCynergiUser(
         username = authentication.name,
         roles = authentication.attributes[tokenConfiguration.rolesName] as MutableCollection<String>,
         level = (authentication.attributes["level"] as Long).toInt(),
         companyId = authentication.attributes["companyId"] as Long,
         userId = authentication.attributes["userId"] as Long
      )
   }

   fun encodeUserDetails(builder: JWTClaimsSet.Builder, cynergiUser: AuthenticatedCynergiUser) {
      builder.claim("level", cynergiUser.level)
      builder.claim("companyId", cynergiUser.companyId)
      builder.claim("userId", cynergiUser.userId)
   }
}
