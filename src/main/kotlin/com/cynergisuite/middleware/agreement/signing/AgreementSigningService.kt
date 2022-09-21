package com.cynergisuite.middleware.agreement.signing

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.agreement.signing.infrastructure.AgreementSigningPageRequest
import com.cynergisuite.middleware.agreement.signing.infrastructure.AgreementSigningRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class AgreementSigningService(
   private val agreementSigningRepository: AgreementSigningRepository,
   private val agreementSigningValidator: AgreementSigningValidator,
) {

   fun fetchById(id: UUID, company: CompanyEntity): AgreementSigningDTO? =
      agreementSigningRepository.findOne(id, company)?.let { AgreementSigningDTO(it) }

   fun fetchByCustomerAndAgreement(company: CompanyEntity, customerNumber: Int, agreementNumber: Int): AgreementSigningDTO? =
      agreementSigningRepository.findOneByCustomerAndAgreement(company, customerNumber, agreementNumber)?.let { AgreementSigningDTO(it) }

   fun fetchAll(pageRequest: AgreementSigningPageRequest, company: CompanyEntity): Page<AgreementSigningDTO> {
      val signingAgreements = agreementSigningRepository.findAll(pageRequest, company)

      return signingAgreements.toPage { agreement ->
         AgreementSigningDTO(
            agreement
         )
      }
   }

   fun create(dto: AgreementSigningDTO, company: CompanyEntity): AgreementSigningDTO {
      val validAgreementToSign = agreementSigningValidator.validateCreate(dto, company)
      val signingAgreement = agreementSigningRepository.insert(validAgreementToSign)

      return AgreementSigningDTO(signingAgreement)
   }

   fun update(dto: AgreementSigningDTO, company: CompanyEntity): AgreementSigningDTO {
      val existingAgreement = agreementSigningValidator.validateUpdate(dto, company)

      val updated = agreementSigningRepository.update(existingAgreement)

      return AgreementSigningDTO(updated)
   }
}
