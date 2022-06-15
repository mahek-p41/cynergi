package com.cynergisuite.middleware.agreement.signing

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.agreement.signing.infrastructure.AgreementSigningRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class AgreementSigningValidator @Inject constructor(
   private val agreementSigningRepository: AgreementSigningRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AgreementSigningValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: AgreementSigningDTO, company: CompanyEntity): AgreementSigningEntity {
      logger.debug("Validating Create Agreement Signing record {}", dto)

      doValidation { errors ->
         val storeNumber = dto.store!!.number

         if (storeNumber != null && !storeRepository.exists(number = storeNumber, company = company)) {
            errors.add(ValidationError("storeNumber", NotFound(storeNumber)))
         }
      }

      return AgreementSigningEntity(
         id = dto.id,
         company = dto.company!!,
         store = storeRepository.findOne(number = dto.store!!.number!!, company = company)!!,
         primaryCustomerNumber = dto.primaryCustomerNumber!!,
         secondaryCustomerNumber = dto.secondaryCustomerNumber!!,
         agreementNumber = dto.agreementNumber!!,
         agreementType = dto.agreementType!!,
         statusId = dto.statusId!!,
         externalSignatureId = dto.externalSignatureId!!,
      )
   }

   @Throws(ValidationException::class)
   fun validateUpdate(dto: AgreementSigningDTO, company: CompanyEntity): AgreementSigningEntity {
      logger.debug("Validating Update Agreement Signing record {}", dto)

      doValidation { errors ->
         val id = dto.id!!
         // TODO Do we need an agmt signing status service?

         val existingAgreement = agreementSigningRepository.findOne(id, company)

         //If it does not exist, that doesn't automatically mean we should insert, so how to handle upsert stuff?
         //We make sure the error sent back explains what happened. In the case of the document upload script,
         //if it gets back that the agmt does not yet exist, then it can decide to kick off an insert instead.
         if (existingAgreement == null) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }

      return AgreementSigningEntity(
         id = dto.id,
         company = dto.company!!,
         store = storeRepository.findOne(number = dto.store!!.number!!, company = company)!!,
         primaryCustomerNumber = dto.primaryCustomerNumber!!,
         secondaryCustomerNumber = dto.secondaryCustomerNumber!!,
         agreementNumber = dto.agreementNumber!!,
         agreementType = dto.agreementType!!,
         statusId = dto.statusId!!,
         externalSignatureId = dto.externalSignatureId!!,
      )
   }
}
