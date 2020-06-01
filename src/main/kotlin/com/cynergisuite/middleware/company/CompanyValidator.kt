package com.cynergisuite.middleware.company

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class CompanyValidator @Inject constructor(
   private val companyRepository: CompanyRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(CompanyValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid companyVO: CompanyValueObject): CompanyEntity {
      logger.trace("Validating Save Company {}", companyVO)

      doValidation { errors ->
         doSharedValidation(errors, companyVO)
      }

      return CompanyEntity(companyVO)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, companyVO: CompanyValueObject): CompanyEntity {
      logger.trace("Validating Update Company {}", companyVO)
      val companyEntity = companyRepository.findOne(id = id)

      doValidation { errors ->
         companyEntity ?: errors.add(ValidationError("id", NotFound(id)))
         doSharedValidation(errors, companyVO, id)
      }

      return CompanyEntity(companyVO)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, companyVO: CompanyValueObject, id: Long? = null) {
      if (companyRepository.duplicate(id = id, clientId = companyVO.clientId)) {
         errors.add(ValidationError("clientId", Duplicate(companyVO.clientId.toString())))
      }

      if (companyRepository.duplicate(id = id, datasetCode = companyVO.datasetCode)) {
         errors.add(ValidationError("datasetCode", Duplicate(companyVO.datasetCode.toString())))
      }
   }
}
