package com.cynergisuite.middleware.sign.here.token

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.sign.here.token.infrastructure.SignHereTokenRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class SignHereTokenValidator @Inject constructor(
   private val signHereTokenRepository: SignHereTokenRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(SignHereTokenValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: SignHereTokenDTO, company: CompanyEntity): SignHereTokenEntity {
      logger.debug("Validating Create Agreement Signing record {}", dto)

      doValidation { errors ->
         val storeNumber = dto.store?.number

         if (storeNumber != null && !storeRepository.exists(number = storeNumber, company = company)) {
            errors.add(ValidationError("storeNumber", NotFound(storeNumber)))
         }
      }

      return SignHereTokenEntity(
         id = dto.id,
         company = company,
         store = storeRepository.findOne(number = dto.store!!.number!!, company = company)!!,
         token = dto.token!!,
      )
   }

   @Throws(ValidationException::class)
   fun validateUpdate(dto: SignHereTokenDTO, company: CompanyEntity): SignHereTokenEntity {
      logger.debug("Validating Update Agreement Signing record {}", dto)

      doValidation { errors ->
         val id = dto.id!!

         val existingAgreement = signHereTokenRepository.findOne(id, company)

         if (existingAgreement == null) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }

      return SignHereTokenEntity(
         id = dto.id,
         company = company,
         store = storeRepository.findOne(number = dto.store!!.number!!, company = company)!!,
         token = dto.token!!,
      )
   }
}
