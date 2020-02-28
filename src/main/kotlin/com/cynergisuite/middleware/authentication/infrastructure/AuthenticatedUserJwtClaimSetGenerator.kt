package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.authentication.infrastructure.JWTDetailKeys.*
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
   COMPANY_ID("ci"),
   DEPARTMENT("dp");
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
            ?.claim(COMPANY_ID.key, userDetails.myCompany().myId())
            ?.claim(DEPARTMENT.key, userDetails.myDepartment()?.myCode())
            ?.claim(STORE_NUMBER.key, userDetails.myLocation().myNumber())
      }

      logger.debug("Finished populating JWT with {}", userDetails)
   }

   fun reversePopulateWithUserDetails(authentication: Authentication): User {
      logger.debug("Deserializing {} into an User {}", authentication)

      val employeeId = authentication.attributes[EMPLOYEE_ID.key]?.let { Objects.toString(it).toLong() } ?: throw Exception("Unable to find employee ID")
      val employeeType = authentication.attributes[EMPLOYEE_TYPE.key]?.let { Objects.toString(it) } ?: throw Exception("Unable to find employee type")
      val employeeNumber = authentication.attributes["sub"].let { Objects.toString(it).toInt() } // sub is a subject which is encoded by the framework
      val companyId = authentication.attributes[COMPANY_ID.key]?.let { Objects.toString(it).toLong() } ?: throw Exception("Unable to find company ID")
      val departmentCode = authentication.attributes[DEPARTMENT.key]?.let { Objects.toString(it) }
      val storeNumber = authentication.attributes[STORE_NUMBER.key]?.let { Objects.toString(it).toInt() } ?: throw Exception("Unable to find store number")

      val (company, department, location) = authenticationRepository.findCredentialComponents(companyId, departmentCode, storeNumber)

      return AuthenticatedUser(
         id = employeeId,
         type = employeeType,
         number = employeeNumber,
         company = company,
         department = department,
         location = location
      )
   }
}
