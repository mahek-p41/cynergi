package com.cynergisuite.middleware.sign.here

import com.cynergisuite.middleware.sign.here.agreement.SignHereAgreementPage
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken

/**
 * Declarative HTTP Client done with Micronaut compile time generation
 * https://docs.micronaut.io/latest/guide/#clientAnnotation
 */
@Client("\${sign.here.please.host}/api/")
interface SignHereClient {

   @Post("login/token")
   fun login(signHereToken: SignHereTokenDto): BearerAccessRefreshToken

   @Get("document/requested{?pageRequest*}", processes = [APPLICATION_JSON])
   fun fetchAgreements(
      @Header(name = "Authorization") token: String,
      @QueryValue("pageRequest") pageRequest: DocumentPageRequest,
   ): SignHereAgreementPage
}
