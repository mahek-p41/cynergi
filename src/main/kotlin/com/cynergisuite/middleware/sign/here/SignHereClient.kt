package com.cynergisuite.middleware.sign.here

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.sign.here.agreement.SignHereAgreementPage
import com.cynergisuite.middleware.sign.here.associated.SignHereAssociatedPage
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.hc.client5.http.classic.methods.HttpDelete
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.protocol.HttpClientContext
import org.apache.hc.core5.http.io.entity.StringEntity
import java.io.OutputStream
import java.util.UUID

@Singleton
class SignHereClient @Inject constructor(
   private val apacheClient:  org.apache.hc.client5.http.classic.HttpClient,
   @Value("\${sign.here.please.client.base-uri}") private val baseUri: String,
   private val objectMapper: ObjectMapper,
) {

   fun login(signHereToken: SignHereTokenDto): BearerAccessRefreshToken {
      val request = HttpPost("${baseUri}/api/login/token").apply {
         addHeader("Content-Type", "application/json")
         entity = StringEntity(objectMapper.writeValueAsString(signHereToken))
      }

      return apacheClient.execute(request, HttpClientContext.create()) { response ->
         objectMapper.readValue(response.entity.content, BearerAccessRefreshToken::class.java)
      }
   }

   fun fetchAgreements(token: String, pageRequest: DocumentPageRequest): SignHereAgreementPage {
      val request = HttpGet("${baseUri}/api/document/requested${pageRequest}").apply { addHeader("Authorization", token) }

      return apacheClient.execute(request, HttpClientContext.create()) { response ->
         objectMapper.readValue(response.entity.content, SignHereAgreementPage::class.java)
      }
   }

   fun fetchAssociated(token: String, signatureRequestedId: UUID, pageRequest: StandardPageRequest): SignHereAssociatedPage {
      val request = HttpGet("${baseUri}/api/document/requested/${signatureRequestedId}${pageRequest}").apply { addHeader("Authorization", token) }

      return apacheClient.execute(request, HttpClientContext.create()) { response ->
         objectMapper.readValue(response.entity.content, SignHereAssociatedPage::class.java)
      }
   }

   fun retrieveDocument(token: String, signatureRequestedId: UUID, os: OutputStream) {
      val request = HttpGet("${baseUri}/api/document/detail/archive/${signatureRequestedId}").apply { addHeader("Authorization", token) }

      apacheClient.execute(request, HttpClientContext.create()) { response ->
         response.entity.content.use { input ->
            input.copyTo(os)
         }
      }
   }

   fun cancelAssociated(token: String, signatureRequestedId: UUID) {
      val request = HttpDelete("${baseUri}/api/document/${signatureRequestedId}").apply { addHeader("Authorization", token) }

      apacheClient.execute(request, HttpClientContext.create()) { _ ->
         // do nothing
      }
   }
}
