package com.cynergisuite.middleware.division

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.employee.infrastructure.SimpleEmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DivisionValidator @Inject constructor(
   private val divisionRepository: DivisionRepository,
   private val simpleEmployeeRepository: SimpleEmployeeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(DivisionValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(divisionDTO: DivisionDTO, company: CompanyEntity): DivisionEntity {
      logger.trace("Validating Save Division {}", divisionDTO)
      val divisionalManager = simpleEmployeeRepository.findOne(divisionDTO.divisionalManager?.id!!, company)

      doValidation { errors ->
         divisionalManager ?: errors.add(ValidationError("dto.divisionalManager.id", NotFound(divisionDTO.divisionalManager?.id!!)))
      }

      return DivisionEntity(dto = divisionDTO, company = company, divisionalManager = divisionalManager)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: UUID, divisionDTO: DivisionDTO, company: CompanyEntity): DivisionEntity {
      logger.trace("Validating Update Division {}", divisionDTO)
      val divisionalManager = simpleEmployeeRepository.findOne(divisionDTO.divisionalManager?.id!!, company)
      val existingDivision = divisionRepository.findOne(id, company) ?: throw NotFoundException(id)

      doValidation { errors ->
         divisionalManager ?: errors.add(ValidationError("dto.divisionalManager.id", NotFound(divisionDTO.divisionalManager?.id!!)))
      }

      return DivisionEntity(existingDivision.id, divisionDTO, company, divisionalManager)
   }
}
