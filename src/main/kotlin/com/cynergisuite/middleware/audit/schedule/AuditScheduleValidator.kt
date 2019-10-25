package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.infrastructure.ScheduleCommandTypeRepository
import com.cynergisuite.middleware.schedule.type.infrastructure.ScheduleTypeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import javax.inject.Singleton

@Singleton
class AuditScheduleValidator(
   private val departmentRepository: DepartmentRepository,
   private val scheduleCommandTypeRepository: ScheduleCommandTypeRepository,
   private val scheduleTypeRepository: ScheduleTypeRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {

   fun validateCreate(dto: AuditScheduleCreateDataTransferObject): Triple<ScheduleEntity, List<StoreEntity>, DepartmentEntity> {
      doValidation {

      }

      val stores = mutableListOf<StoreEntity>()
      val department: DepartmentEntity = departmentRepository.findOne(dto.departmentAccess!!.id!!)!!
      val arguments = mutableListOf(
         ScheduleArgumentEntity(
            department.code,
            "departmentAccess"
         )
      )

      for (storeIn: SimpleIdentifiableDataTransferObject in dto.stores) {
         val store = storeRepository.findOne(storeIn.dtoId()!!)

         arguments.add(
            ScheduleArgumentEntity(
               store!!.number.toString(),
               "storeNumber"
            )
         )
      }

      return Triple(
         ScheduleEntity(
            title = dto.title!!,
            description = dto.description,
            schedule = dto.schedule!!.name,
            command = scheduleCommandTypeRepository.findByValue("AuditSchedule"),
            type = scheduleTypeRepository.findByValue("WEEKLY"),
            arguments = arguments
         ),
         stores,
         department
      )
   }
}
