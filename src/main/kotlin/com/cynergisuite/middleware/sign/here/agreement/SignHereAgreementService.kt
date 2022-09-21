package com.cynergisuite.middleware.sign.here.agreement

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.withUri
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.SignHerePleaseNotEnabled
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.sign.here.DocumentPageRequest
import com.cynergisuite.middleware.sign.here.SignHereClient
import com.cynergisuite.middleware.sign.here.SignHereTokenDto
import com.cynergisuite.middleware.sign.here.associated.SignHereAssociatedPage
import com.cynergisuite.middleware.sign.here.token.infrastructure.SignHereTokenRepository
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.client.ProxyHttpClient
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class SignHereAgreementService @Inject constructor(
   private val signHereClient: SignHereClient,
   private val signHereTokenRepository: SignHereTokenRepository,
   private val signHereProxyClient: ProxyHttpClient,
   @Value("\${sign.here.please.scheme}") val signHereScheme: String,
   @Value("\${sign.here.please.host}") val signHereHost: String,
   @Value("\${sign.here.please.port}") val signHerePort: Int,
) {
   private val logger = LoggerFactory.getLogger(SignHereAgreementService::class.java)

   fun findRequestsByStore(location: Location, company: CompanyEntity, documentPageRequest: DocumentPageRequest): SignHereAgreementPage {
      val token = signHereTokenRepository.findOneByStoreNumber(location.myNumber(), company) ?: throw ValidationException(ValidationError(localizationCode = SignHerePleaseNotEnabled(location, company)))
      val signHereToken = SignHereTokenDto(token)
      val signHereLogin = signHereClient.login(signHereToken)

      return signHereClient.fetchAgreements("Bearer ${signHereLogin.accessToken}", documentPageRequest)
   }

   fun findAssociated(location: Location, company: CompanyEntity, signatureRequestedId: UUID, pageRequest: StandardPageRequest): SignHereAssociatedPage {
      val token = signHereTokenRepository.findOneByStoreNumber(location.myNumber(), company) ?: throw ValidationException(ValidationError(localizationCode = SignHerePleaseNotEnabled(location, company)))
      val signHereToken = SignHereTokenDto(token)
      val signHereLogin = signHereClient.login(signHereToken)

      return signHereClient.fetchAssociated("Bearer ${signHereLogin.accessToken}", signatureRequestedId, pageRequest)
   }

   fun retrieveDocument(location: Location, company: CompanyEntity, documentId: UUID, httpRequest: HttpRequest<*>): Publisher<MutableHttpResponse<*>> {
      val token = signHereTokenRepository.findOneByStoreNumber(location.myNumber(), company) ?: throw ValidationException(ValidationError(localizationCode = SignHerePleaseNotEnabled(location, company)))
      val signHereToken = SignHereTokenDto(token)
      val signHereLogin = signHereClient.login(signHereToken)

      logger.debug("token {}", token)
      logger.debug("signHereToken {}", signHereToken)
      logger.debug("signHereLogin {}", signHereLogin.accessToken)

      return signHereProxyClient.proxy(
         httpRequest.mutate()
            .headers { headers -> headers.remove("Authorization") }
            .bearerAuth(signHereLogin.accessToken)
            .withUri {
               scheme(signHereScheme)
               host(signHereHost)
               port(signHerePort)
               replacePath(StringUtils.remove(httpRequest.path, "/sign/here/agreement"))
            }
      )
   }
}
