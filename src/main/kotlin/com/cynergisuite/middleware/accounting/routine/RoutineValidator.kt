package com.cynergisuite.middleware.accounting.routine

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.routine.infrastructure.RoutineRepository
import com.cynergisuite.middleware.accounting.routine.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.Duplicate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineValidator @Inject constructor(
   private val routineRepository: RoutineRepository,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(RoutineValidator::class.java)

   fun validateCreate(dto: RoutineDTO, company: Company): RoutineEntity {
      logger.trace("Validating Create Routine{}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: Long, dto: RoutineDTO, company: Company): RoutineEntity {
      logger.debug("Validating Update Routine{}", dto)

      val existingRoutine = routineRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, existingRoutine)
   }

   private fun doSharedValidation(dto: RoutineDTO, company: Company, existingRoutine: RoutineEntity? = null): RoutineEntity {
      val overallPeriodEntity = overallPeriodTypeRepository.findOne(dto.overallPeriod!!.value) ?: throw NotFoundException(dto.overallPeriod!!.value)
      val routineExists = routineRepository.exists(company, overallPeriodEntity.id, dto.period!!)

      doValidation { errors ->
         if ((existingRoutine == null && routineExists) || (existingRoutine != null && existingRoutine.id != dto.id)) errors.add(ValidationError("id", Duplicate(dto.id)))
      }

      return RoutineEntity(
         dto,
         overallPeriodEntity
      )
   }
}
