package com.cynergisuite.middleware.company

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AddressNeedsUpdated
import com.cynergisuite.middleware.localization.Duplicate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CompanyValidator @Inject constructor(
   private val companyRepository: CompanyRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(CompanyValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(companyDTO: CompanyDTO): CompanyEntity {
      logger.trace("Validating Save Company {}", companyDTO)

      doValidation { errors ->
         doSharedValidation(errors, companyDTO)
      }

      return CompanyEntity(companyDTO)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: UUID, companyDTO: CompanyDTO): Pair<CompanyEntity, CompanyEntity> {
      logger.trace("Validating Update Company {}", companyDTO)
      val existingEntity = companyRepository.findOne(id = id) ?: throw NotFoundException(id)

      doValidation { errors ->

         if (existingEntity.address?.id != null) { // have existing address
            if (companyDTO.address?.id == null && !companyDTO.address?.name.isNullOrBlank()) { // just update existing address rather than create new one
               errors.add(ValidationError("address.id", AddressNeedsUpdated()))
            }
         }

         doSharedValidation(errors, companyDTO, id)
      }

      return Pair(existingEntity, CompanyEntity(companyDTO))
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, companyDTO: CompanyDTO, id: UUID? = null) {
      if (companyRepository.duplicate(id = id, clientId = companyDTO.clientId)) {
         errors.add(ValidationError("clientId", Duplicate(companyDTO.clientId.toString())))
      }

      if (companyRepository.duplicate(id = id, datasetCode = companyDTO.datasetCode)) {
         errors.add(ValidationError("datasetCode", Duplicate(companyDTO.datasetCode.toString())))
      }
   }
}
