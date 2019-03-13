package com.hightouchinc.cynergi.middleware.authentication

import com.nimbusds.jwt.JWTClaimsSet
import io.micronaut.context.annotation.Replaces
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.token.config.TokenConfiguration
import io.micronaut.security.token.jwt.generator.claims.ClaimsAudienceProvider
import io.micronaut.security.token.jwt.generator.claims.JWTClaimsSetGenerator
import io.micronaut.security.token.jwt.generator.claims.JwtIdGenerator
import javax.inject.Singleton

@Singleton
@Replaces(JWTClaimsSetGenerator::class)
class CynergiCustomJwtClaimsSetGenerator(
   tokenConfiguration: TokenConfiguration?,
   jwtIdGenerator: JwtIdGenerator?,
   claimsAudienceProvider: ClaimsAudienceProvider?,
   private val authenticationService: AuthenticationService
) : JWTClaimsSetGenerator(tokenConfiguration, jwtIdGenerator, claimsAudienceProvider) {

   override fun populateWithUserDetails(builder: JWTClaimsSet.Builder, userDetails: UserDetails) {
      super.populateWithUserDetails(builder, userDetails)

      if (userDetails is AuthenticatedCynergiUser) {
         authenticationService.encodeUserDetails(builder, userDetails)
      }
   }
}
