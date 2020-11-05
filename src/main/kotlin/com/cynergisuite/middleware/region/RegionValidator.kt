package com.cynergisuite.middleware.region

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.SimpleEmployeeRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.MustMatchPathVariable
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionValidator @Inject constructor(
   private val regionRepository: RegionRepository,
   private val divisionRepository: DivisionRepository,
   private val simpleEmployeeRepository: SimpleEmployeeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(RegionValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: RegionDTO, company: Company): RegionEntity {
      logger.trace("Validating Save Region {}", dto)
      val regionalManager = simpleEmployeeRepository.findOne(dto.regionalManager?.id!!, company)
      val division = divisionRepository.findOne(dto.division?.id!!, company)

      doValidation { errors ->
         doShareValidator(regionalManager, errors, dto, division)
      }

      return RegionEntity(dto, division!!, regionalManager)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, dto: RegionDTO, company: Company): RegionEntity {
      logger.trace("Validating Update Region {}", dto)
      val regionalManager = simpleEmployeeRepository.findOne(dto.regionalManager?.id!!, company)
      val division = divisionRepository.findOne(dto.division?.id!!, company)

      doValidation { errors ->
         if (id != dto.myId()) errors.add(ValidationError("dto.id", MustMatchPathVariable("Id")))
         regionRepository.findOne(id, company) ?: errors.add(ValidationError("dto.id", NotFound(id)))
         doShareValidator(regionalManager, errors, dto, division)
      }

      return RegionEntity(dto, division!!, regionalManager)
   }

   private fun doShareValidator(regionalManager: EmployeeEntity?, errors: MutableSet<ValidationError>, regionDTO: RegionDTO, division: DivisionEntity?) {
      regionalManager ?: errors.add(ValidationError("dto.regionalManager.id", NotFound(regionDTO.regionalManager?.id!!)))
      division ?: errors.add(ValidationError("dto.division.id", NotFound(regionDTO.division?.id!!)))
   }
}
