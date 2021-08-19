package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.extensions.toUuid
import com.cynergisuite.middleware.authentication.infrastructure.JWTDetailKeys.COMPANY_ID
import com.cynergisuite.middleware.authentication.infrastructure.JWTDetailKeys.EMPLOYEE_ID
import com.cynergisuite.middleware.authentication.infrastructure.JWTDetailKeys.EMPLOYEE_TYPE
import com.cynergisuite.middleware.authentication.infrastructure.JWTDetailKeys.STORE_NUMBER
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.authentication.user.infrastructure.AuthenticationRepository
import com.nimbusds.jwt.JWTClaimsSet.Builder
import io.micronaut.context.annotation.Replaces
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.token.config.TokenConfiguration
import io.micronaut.security.token.jwt.generator.claims.ClaimsAudienceProvider
import io.micronaut.security.token.jwt.generator.claims.JWTClaimsSetGenerator
import io.micronaut.security.token.jwt.generator.claims.JwtIdGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

private enum class JWTDetailKeys(
   val key: String
) {
   EMPLOYEE_ID("id"),
   EMPLOYEE_TYPE("tp"),
   STORE_NUMBER("sn"),
   COMPANY_ID("ci");
}

@Singleton
@Replaces(bean = JWTClaimsSetGenerator::class)
class AuthenticatedUserJwtClaimSetGenerator @Inject constructor(
   private val authenticationRepository: AuthenticationRepository,

   tokenConfiguration: TokenConfiguration,
   jwtIdGenerator: JwtIdGenerator?,
   claimsAudienceProvider: ClaimsAudienceProvider?,
   applicationConfiguration: ApplicationConfiguration?
) : JWTClaimsSetGenerator(tokenConfiguration, jwtIdGenerator, claimsAudienceProvider, applicationConfiguration) {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticatedUserJwtClaimSetGenerator::class.java)

   override fun populateWithUserDetails(builder: Builder?, userDetails: UserDetails?) {
      super.populateWithUserDetails(builder, userDetails)

      logger.debug("Populating JWT with {}", userDetails)

      if (userDetails is User) {
         builder
            ?.claim(EMPLOYEE_ID.key, userDetails.myId())
            ?.claim(EMPLOYEE_TYPE.key, userDetails.myEmployeeType())
            ?.claim(COMPANY_ID.key, userDetails.myCompany().id.toString())
            ?.claim(STORE_NUMBER.key, userDetails.myLocation().myNumber())
      }

      logger.debug("Finished populating JWT with {}", userDetails)
   }

   fun reversePopulateWithUserDetails(authentication: Authentication): User {
      val employeeId = authentication.attributes[EMPLOYEE_ID.key]?.let { Objects.toString(it).toLong() } ?: throw Exception("Unable to find employee ID")
      val employeeType = authentication.attributes[EMPLOYEE_TYPE.key]?.let { Objects.toString(it) } ?: throw Exception("Unable to find employee type")
      val employeeNumber = authentication.attributes["sub"]?.let { Objects.toString(it).toInt() } ?: throw Exception("Unable to find employee number")
      val companyId = authentication.attributes[COMPANY_ID.key]?.let { Objects.toString(it).toUuid() } ?: throw Exception("Unable to find company ID")
      val storeNumber = authentication.attributes[STORE_NUMBER.key]?.let { Objects.toString(it).toInt() } ?: throw Exception("Unable to find store number")

      return authenticationRepository.findUser(employeeId, employeeType, employeeNumber, companyId, storeNumber) // this should be cached so the lookup should only be required once per user login
   }
}
