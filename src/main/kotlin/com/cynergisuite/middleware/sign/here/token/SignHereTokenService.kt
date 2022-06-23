package com.cynergisuite.middleware.sign.here.token

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.sign.here.token.infrastructure.SignHereTokenRepository
import jakarta.inject.Singleton
import java.util.*

@Singleton
class SignHereTokenService(
   private val signHereTokenRepository: SignHereTokenRepository,
   private val signHereTokenValidator: SignHereTokenValidator,
) {

   fun fetchById(id: UUID, company: CompanyEntity): SignHereTokenDTO? =
      signHereTokenRepository.findOne(id, company)?.let { SignHereTokenDTO(it) }

   fun fetchByStoreNumber(storeNumber: Int, company: CompanyEntity): SignHereTokenDTO? =
      signHereTokenRepository.findOneByStoreNumber(storeNumber, company)?.let {
         SignHereTokenDTO(
            it
         )
      }

   fun create(dto: SignHereTokenDTO, company: CompanyEntity): SignHereTokenDTO {
      val validAgreementToSign = signHereTokenValidator.validateCreate(dto, company)
      val signingAgreement = signHereTokenRepository.insert(validAgreementToSign)

      return SignHereTokenDTO(signingAgreement)
   }

   fun update(dto: SignHereTokenDTO, company: CompanyEntity): SignHereTokenDTO {
      val existingAgreement = signHereTokenValidator.validateUpdate(dto, company)

      val updated = signHereTokenRepository.update(existingAgreement)

      return SignHereTokenDTO(updated)
   }
}
