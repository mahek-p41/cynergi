package com.cynergisuite.middleware.area

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.area.infrastructure.ModuleRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.ConfigAlreadyExist
import com.cynergisuite.middleware.localization.NotFound
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class ModuleValidator @Inject constructor(
   private val moduleRepository: ModuleRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(ModuleValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(company: CompanyEntity, moduleDTO: ModuleDTO): ModuleType {
      val moduleTypeId = moduleDTO.id!!
      val moduleEntity = moduleRepository.findOne(moduleTypeId, company)
      val configExists = moduleRepository.configExists(moduleTypeId, company)
      logger.trace("Validating ModuleTypeId {}", moduleTypeId)

      doValidation { errors ->
         moduleEntity ?: errors.add(ValidationError("id", NotFound(moduleTypeId)))
         if (configExists) errors.add(ValidationError("id", ConfigAlreadyExist("Config for module $moduleTypeId")))
      }
      return moduleEntity!!.copy(level = moduleDTO.level)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(company: CompanyEntity, moduleDTO: ModuleDTO): ModuleType {
      val moduleTypeId = moduleDTO.id!!
      val moduleEntity = moduleRepository.findOne(moduleTypeId, company)
      val configExists = moduleRepository.configExists(moduleTypeId, company)
      logger.trace("Validating ModuleTypeId {}", moduleTypeId)

      doValidation { errors ->
         moduleEntity ?: errors.add(ValidationError("id", NotFound(moduleTypeId)))
         if (!configExists) errors.add(ValidationError("id", NotFound("Config for module $moduleTypeId")))
      }
      return moduleEntity!!.copy(level = moduleDTO.level)
   }
}
