package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.type.infrastructure.ScheduleTypeRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import javax.inject.Singleton

@Singleton
class AuditScheduleValidator(
   private val departmentRepository: DepartmentRepository,
   private val scheduleTypeRepository: ScheduleTypeRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   fun validateCreate(auditScheduleCreate: AuditScheduleCreateDataTransferObject): AuditScheduleEntity {
      val storeId = auditScheduleCreate.store.id!!
      val departmentId = auditScheduleCreate.departmentAccess.id!!
      val scheduleTypeId = auditScheduleCreate.schedule.type.id!!

      doValidation { errors ->
         if ( !storeRepository.exists(storeId) ) {
            errors.add(ValidationError("store", NotFound(storeId)))
         }

         if ( !departmentRepository.exists(departmentId) ) {
            errors.add(ValidationError("departmentAccess", NotFound(departmentId)))
         }

         if ( !scheduleTypeRepository.exists())
      }

      return AuditScheduleEntity(
         store = storeRepository.findOne(storeId)!!,
         departmentAccess = departmentRepository.findOne(departmentId),
         schedule = ScheduleEntity(
            title = auditScheduleCreate.schedule.title!!,
            description = auditScheduleCreate.schedule.description!!,
            schedule = auditScheduleCreate.schedule!!,
            command = "AuditCreator",
            type =
         )
      )
   }
}
