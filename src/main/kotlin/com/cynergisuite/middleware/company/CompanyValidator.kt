package com.cynergisuite.middleware.company

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AddressNeedsUpdated
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotImplemented
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class CompanyValidator @Inject constructor(
   private val addressRepository: AddressRepository,
   private val companyRepository: CompanyRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(CompanyValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid companyDTO: CompanyDTO): CompanyEntity {
      logger.trace("Validating Save Company {}", companyDTO)

      doValidation { errors ->
         doSharedValidation(errors, companyDTO)
      }

      return CompanyEntity(companyDTO)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, companyDTO: CompanyDTO): CompanyEntity {
      logger.trace("Validating Update Company {}", companyDTO)
      val companyEntity = companyRepository.findOne(id = id)

      doValidation { errors ->
         companyEntity ?: errors.add(ValidationError("id", NotFound(id)))

         // address validation
         if (companyDTO.address?.id == null && companyEntity?.address != null) {

            companyRepository.removeAddressFromCompany(companyDTO.myId()!!)

            val addressIdToDelete = companyEntity.address.myId()
            addressRepository.delete(addressIdToDelete!!)

         } else if (companyDTO.address?.id != null) {
            if (companyDTO.address?.name != companyEntity?.address?.name) {
               addressRepository.update(companyDTO.address!!)
            }
         }

         if (companyDTO.address?.id == null && companyDTO.address?.name != null) {
            errors.add(ValidationError("address", AddressNeedsUpdated(companyDTO.address!!)))
         }

         doSharedValidation(errors, companyDTO, id)
      }

      return CompanyEntity(companyDTO)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, companyDTO: CompanyDTO, id: Long? = null) {
      if (companyRepository.duplicate(id = id, clientId = companyDTO.clientId)) {
         errors.add(ValidationError("clientId", Duplicate(companyDTO.clientId.toString())))
      }

      if (companyRepository.duplicate(id = id, datasetCode = companyDTO.datasetCode)) {
         errors.add(ValidationError("datasetCode", Duplicate(companyDTO.datasetCode.toString())))
      }
   }
}
