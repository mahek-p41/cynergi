package com.cynergisuite.middleware.division

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.employee.infrastructure.SimpleEmployeeRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class DivisionValidator @Inject constructor(
   private val divisionRepository: DivisionRepository,
   private val simpleEmployeeRepository: SimpleEmployeeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(DivisionValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid divisionDTO: DivisionDTO, company: Company): DivisionEntity {
      logger.trace("Validating Save Division {}", divisionDTO)
      val divisionalManager = simpleEmployeeRepository.findOne(divisionDTO.divisionalManager?.id!!, "eli", company)

      doValidation { errors ->
         divisionalManager ?: errors.add(ValidationError("dto.divisionalManager.id", NotFound(divisionDTO.divisionalManager?.id!!)))
      }

      return DivisionEntity(divisionDTO, company, divisionalManager)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, divisionDTO: DivisionDTO, company: Company): DivisionEntity {
      logger.trace("Validating Update Division {}", divisionDTO)
      val divisionalManager = simpleEmployeeRepository.findOne(divisionDTO.divisionalManager?.id!!, "eli", company)

      doValidation { errors ->
         divisionRepository.findOne(id, company) ?: errors.add(ValidationError("dto.id", NotFound(id)))
         divisionalManager ?: errors.add(ValidationError("dto.divisionalManager.id", NotFound(divisionDTO.divisionalManager?.id!!)))
      }

      return DivisionEntity(divisionDTO, company, divisionalManager)
   }
}
