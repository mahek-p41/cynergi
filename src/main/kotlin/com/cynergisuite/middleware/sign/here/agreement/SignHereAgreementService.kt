package com.cynergisuite.middleware.sign.here.agreement

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.SignHerePleaseNotEnabled
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.sign.here.DocumentPageRequest
import com.cynergisuite.middleware.sign.here.SignHereClient
import com.cynergisuite.middleware.sign.here.SignHereTokenDto
import com.cynergisuite.middleware.sign.here.token.infrastructure.SignHereTokenRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class SignHereAgreementService @Inject constructor(
   private val signHereClient: SignHereClient,
   private val signHereTokenRepository: SignHereTokenRepository,
) {

   fun findRequestsByStore(location: Location, company: CompanyEntity, documentPageRequest: DocumentPageRequest): SignHereAgreementPage {
      val token = signHereTokenRepository.findOneByStoreNumber(location.myNumber(), company) ?: throw ValidationException(ValidationError(localizationCode = SignHerePleaseNotEnabled(location, company)))
      val signHereToken = SignHereTokenDto(token)
      val signHereLogin = signHereClient.login(signHereToken)

      return signHereClient.fetchAgreements("Bearer ${signHereLogin.accessToken}", documentPageRequest)
   }
}
