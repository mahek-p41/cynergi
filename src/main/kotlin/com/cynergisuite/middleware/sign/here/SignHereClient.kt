package com.cynergisuite.middleware.sign.here

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.sign.here.agreement.SignHereAgreementPage
import com.cynergisuite.middleware.sign.here.associated.SignHereAssociatedPage
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import java.util.UUID

/**
 * Declarative HTTP Client done with Micronaut compile time generation
 * https://docs.micronaut.io/latest/guide/#clientAnnotation
 */
@Client("\${sign.here.please.client.base-uri}/api/")
interface SignHereClient {

   @Post("login/token")
   @Cacheable("sign-here-please-token-cache")
   fun login(@Body signHereToken: SignHereTokenDto): BearerAccessRefreshToken

   @Get("document/requested{?pageRequest*}", processes = [APPLICATION_JSON])
   fun fetchAgreements(
      @Header("Authorization") token: String,
      @QueryValue("pageRequest") pageRequest: DocumentPageRequest,
   ): SignHereAgreementPage

   @Get("document/requested/{signatureRequestedId}{?pageRequest*}", processes = [APPLICATION_JSON])
   fun fetchAssociated(
      @Header("Authorization") token: String,
      @PathVariable("signatureRequestedId") signatureRequestedId: UUID,
      @QueryValue("pageRequest") pageRequest: StandardPageRequest,
   ): SignHereAssociatedPage
}
