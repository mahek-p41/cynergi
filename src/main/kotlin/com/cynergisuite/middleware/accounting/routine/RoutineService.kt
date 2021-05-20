package com.cynergisuite.middleware.accounting.routine

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.routine.infrastructure.RoutineRepository
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineService @Inject constructor(
   private val routineRepository: RoutineRepository,
   private val routineValidator: RoutineValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): RoutineDTO? =
      routineRepository.findOne(id, company)?.let { RoutineDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<RoutineDTO> {
      val found = routineRepository.findAll(company, pageRequest)

      return found.toPage { routineEntity: RoutineEntity ->
         RoutineDTO(routineEntity)
      }
   }

   fun create(dto: RoutineDTO, company: CompanyEntity): RoutineDTO {
      val toCreate = routineValidator.validateCreate(dto, company)

      return transformEntity(routineRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: RoutineDTO, company: CompanyEntity): RoutineDTO {
      val toUpdate = routineValidator.validateUpdate(id, dto, company)

      return transformEntity(routineRepository.update(toUpdate, company))
   }

   fun openGLAccountsForPeriods(dateRangeDTO: RoutineDateRangeDTO, company: CompanyEntity) =
      routineRepository.openGLAccountsForPeriods(dateRangeDTO, company)

   fun openAPAccountsForPeriods(dateRangeDTO: RoutineDateRangeDTO, company: CompanyEntity) =
      routineRepository.openAPAccountsForPeriods(dateRangeDTO, company)

   private fun transformEntity(routineEntity: RoutineEntity): RoutineDTO {
      return RoutineDTO(routineEntity)
   }
}
