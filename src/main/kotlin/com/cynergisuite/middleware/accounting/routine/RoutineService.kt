package com.cynergisuite.middleware.accounting.routine

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.routine.infrastructure.RoutineRepository
import com.cynergisuite.middleware.company.Company
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineService @Inject constructor(
   private val routineRepository: RoutineRepository,
   private val routineValidator: RoutineValidator
) {

   fun fetchById(id: Long, company: Company): RoutineDTO? =
      routineRepository.findOne(id, company)?.let { RoutineDTO(it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<RoutineDTO> {
      val found = routineRepository.findAll(company, pageRequest)

      return found.toPage { routineEntity: RoutineEntity ->
         RoutineDTO(routineEntity)
      }
   }

   fun create(dto: RoutineDTO, company: Company): RoutineDTO {
      val toCreate = routineValidator.validateCreate(dto, company)

      return transformEntity(routineRepository.insert(toCreate, company))
   }

   fun update(id: Long, dto: RoutineDTO, company: Company): RoutineDTO {
      val toUpdate = routineValidator.validateUpdate(id, dto, company)

      return transformEntity(routineRepository.update(toUpdate, company))
   }

   fun openGLAccountsForPeriods(dateRangeDTO: RoutineDateRangeDTO, company: Company) =
      routineRepository.openGLAccountsForPeriods(dateRangeDTO, company)

   private fun transformEntity(routineEntity: RoutineEntity): RoutineDTO {
      return RoutineDTO(routineEntity)
   }
}
