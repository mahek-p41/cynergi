package com.cynergisuite.middleware.area

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.area.infrastructure.ModuleRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.ConfigAlreadyExist
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class ModuleValidator @Inject constructor(
   private val moduleRepository: ModuleRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(ModuleValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(company: Company, @Valid moduleDTO: ModuleDTO): ModuleType {
      val moduleTypeId = moduleDTO.id!!
      val moduleEntity = moduleRepository.findOne(moduleTypeId, company)
      val isConfigExist = moduleRepository.isConfigExists(moduleTypeId)
      logger.trace("Validating ModuleTypeId {}", moduleTypeId)

      doValidation { errors ->
         moduleEntity ?: errors.add(ValidationError("id", NotFound(moduleTypeId)))
         if (isConfigExist) errors.add(ValidationError("id", ConfigAlreadyExist("Config for module $moduleTypeId")))
      }
      return moduleEntity!!.copy(level = moduleDTO.level)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(company: Company, @Valid moduleDTO: ModuleDTO): ModuleType {
      val moduleTypeId = moduleDTO.id!!
      val moduleEntity = moduleRepository.findOne(moduleTypeId, company)
      val isConfigExists = moduleRepository.isConfigExists(moduleTypeId)
      logger.trace("Validating ModuleTypeId {}", moduleTypeId)

      doValidation { errors ->
         moduleEntity ?: errors.add(ValidationError("id", NotFound(moduleTypeId)))
         if (!isConfigExists) errors.add(ValidationError("id", NotFound("Config for module $moduleTypeId")))
      }
      return moduleEntity!!.copy(level = moduleDTO.level)
   }
}
