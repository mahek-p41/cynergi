package com.cynergisuite.middleware.region

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.SimpleEmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class RegionValidator @Inject constructor(
   private val regionRepository: RegionRepository,
   private val divisionRepository: DivisionRepository,
   private val simpleEmployeeRepository: SimpleEmployeeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(RegionValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: RegionDTO, company: CompanyEntity): RegionEntity {
      logger.trace("Validating Save Region {}", dto)
      val regionalManager = simpleEmployeeRepository.findOne(dto.regionalManager?.id!!, company)
      val division = divisionRepository.findOne(dto.division?.id!!, company)

      doValidation { errors ->
         doShareValidator(regionalManager, errors, dto, division)
      }

      return RegionEntity(dto = dto, division = division!!, regionalManager = regionalManager)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: UUID, dto: RegionDTO, company: CompanyEntity): RegionEntity {
      logger.trace("Validating Update Region {}", dto)
      val regionalManager = simpleEmployeeRepository.findOne(dto.regionalManager?.id!!, company)
      val existingRegion = regionRepository.findOne(id, company) ?: throw NotFoundException(id)
      val division = divisionRepository.findOne(dto.division?.id!!, company)

      doValidation { errors ->
         doShareValidator(regionalManager, errors, dto, division)
      }

      return RegionEntity(existingRegion.id, dto, division!!, regionalManager)
   }

   private fun doShareValidator(regionalManager: EmployeeEntity?, errors: MutableSet<ValidationError>, regionDTO: RegionDTO, division: DivisionEntity?) {
      regionalManager ?: errors.add(ValidationError("dto.regionalManager.id", NotFound(regionDTO.regionalManager?.id!!)))
      division ?: errors.add(ValidationError("dto.division.id", NotFound(regionDTO.division?.id!!)))
   }
}
