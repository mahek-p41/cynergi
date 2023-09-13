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
import io.micronaut.retry.annotation.Retryable
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

   @Retryable(attempts = "3")
   fun findRequestsByStore(location: Location, company: CompanyEntity, documentPageRequest: DocumentPageRequest): SignHereAgreementPage {
      logger.info("findRequestsByStore -> Looking up token for company/location {}/{}", company.id, location.myNumber())
      val token = signHereTokenRepository.findOneByStoreNumber(location.myNumber(), company) ?: throw ValidationException(ValidationError(localizationCode = SignHerePleaseNotEnabled(location, company)))

      logger.info("findRequestsByStore -> Logging into sign here please service")
      val signHereToken = SignHereTokenDto(token)
      val signHereLogin = signHereClient.login(signHereToken)
      logger.info("findRequestsByStore -> Logged into sign here please service")

      val agreements = signHereClient.fetchAgreements("Bearer ${signHereLogin.accessToken}", documentPageRequest)

      logger.info("findRequestsByStore -> successfully loaded agreements")

      return agreements
   }

   @Retryable(attempts = "3")
   fun findAssociated(location: Location, company: CompanyEntity, signatureRequestedId: UUID, pageRequest: StandardPageRequest): SignHereAssociatedPage {
      logger.info("findAssociated -> Looking up token for company/location {}/{}", company.id, location.myNumber())
      val token = signHereTokenRepository.findOneByStoreNumber(location.myNumber(), company) ?: throw ValidationException(ValidationError(localizationCode = SignHerePleaseNotEnabled(location, company)))

      logger.info("findAssociated -> Logging into sign here please service")
      val signHereToken = SignHereTokenDto(token)
      val signHereLogin = signHereClient.login(signHereToken)
      logger.info("findAssociated -> Logged into sign here please service")

      val associated = signHereClient.fetchAssociated("Bearer ${signHereLogin.accessToken}", signatureRequestedId, pageRequest)

      logger.info("findAssociated -> successfully loaded associated")

      return associated
   }

   @Retryable(attempts = "3")
   fun retrieveDocument(location: Location, company: CompanyEntity, documentId: UUID, httpRequest: HttpRequest<*>): Publisher<MutableHttpResponse<*>> {
      logger.info("retrieveDocument -> Looking up token for company/location {}/{}", company.id, location.myNumber())
      val token = signHereTokenRepository.findOneByStoreNumber(location.myNumber(), company) ?: throw ValidationException(ValidationError(localizationCode = SignHerePleaseNotEnabled(location, company)))

      logger.info("retrieveDocument -> Logging into sign here please service")
      val signHereToken = SignHereTokenDto(token)
      val signHereLogin = signHereClient.login(signHereToken)
      logger.info("retrieveDocument -> Logged into sign here please service")

      val result = signHereProxyClient.proxy(
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

      logger.info("retrieveDocument -> successfully loaded document")

      return result
   }

   @Retryable(attempts = "3")
   fun cancelAssociated(location: Location, company: CompanyEntity, signatureRequestedId: UUID) {
      logger.info("cancelAssociated -> Looking up token for company/location {}/{}", company.id, location.myNumber())
      val token = signHereTokenRepository.findOneByStoreNumber(location.myNumber(), company) ?: throw ValidationException(ValidationError(localizationCode = SignHerePleaseNotEnabled(location, company)))

      logger.info("cancelAssociated -> Logging into sign here please service")
      val signHereToken = SignHereTokenDto(token)
      val signHereLogin = signHereClient.login(signHereToken)
      logger.info("cancelAssociated -> Logged into sign here please service")

      val cancelledRecordsCount = signHereClient.cancelAssociated("Bearer ${signHereLogin.accessToken}", signatureRequestedId)

      logger.info("cancelAssociated -> successfully cancelled associated")

      return cancelledRecordsCount
   }
}
