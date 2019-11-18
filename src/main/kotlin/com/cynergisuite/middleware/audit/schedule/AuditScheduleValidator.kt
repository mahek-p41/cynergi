package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.infrastructure.ScheduleCommandTypeRepository
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.infrastructure.ScheduleTypeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import javax.inject.Singleton

@Singleton
class AuditScheduleValidator(
   private val departmentRepository: DepartmentRepository,
   private val scheduleCommandTypeRepository: ScheduleCommandTypeRepository,
   private val scheduleRepository: ScheduleRepository,
   private val scheduleTypeRepository: ScheduleTypeRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {

   fun validateCreate(dto: AuditScheduleCreateUpdateDataTransferObject): Triple<ScheduleEntity, List<StoreEntity>, DepartmentEntity> {
      doValidation { errors -> doSharedValidation(dto, errors) }

      val stores = mutableListOf<StoreEntity>()
      val department: DepartmentEntity = departmentRepository.findOne(dto.department!!.id!!)!!
      val arguments = mutableSetOf(
         ScheduleArgumentEntity(
            department.code,
            "department"
         )
      )

      for (storeIn in dto.stores) {
         val store = storeRepository.findOne(storeIn.myId()!!)!!

         stores.add(store)

         arguments.add(
            ScheduleArgumentEntity(
               store.number.toString(),
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
            arguments = arguments,
            enabled = dto.enabled!!
         ),
         stores,
         department
      )
   }

   fun validateUpdate(dto: AuditScheduleCreateUpdateDataTransferObject): Triple<ScheduleEntity, List<StoreEntity>, DepartmentEntity> {
      doValidation { errors ->
         val scheduleId = dto.id

         if (scheduleId == null) {
            errors.add(ValidationError("id", NotNull("id")))
         } else if (scheduleRepository.doesNotExist(scheduleId)) {
            errors.add(ValidationError("id", NotFound(scheduleId)))
         }

         doSharedValidation(dto, errors)
      }

      val stores = mutableListOf<StoreEntity>()
      val schedule = scheduleRepository.findOne(dto.id!!)!!
      val updateDepartment = departmentRepository.findOne(dto.department!!.id!!)!!
      val existingDepartment: Pair<ScheduleArgumentEntity, DepartmentEntity> = schedule.arguments.first { it.description == "department" }.let { it to departmentRepository.findOneByCode(it.value)!! }
      val existingStores: List<Pair<ScheduleArgumentEntity, StoreEntity>> = schedule.arguments.asSequence()
         .filter { it.description == "storeNumber" }
         .map { it to storeRepository.findOneByNumber(it.value.toInt())!! }
         .sortedBy { it.second.id }
         .toList()
      val updateStores: List<StoreEntity> = dto.stores.asSequence()
         .map { storeRepository.findOne(it.id!!)!! }
         .toList()
      val argsToUpdate = mutableSetOf<ScheduleArgumentEntity>()

      for (updateStore in updateStores) {
         val location = existingStores.binarySearch { it.second.id.compareTo(updateStore.id) }
         val store = if (location > -1) {
            stores.add(existingStores[location].second)
            existingStores[location].first
         } else {
            stores.add(updateStore)
            ScheduleArgumentEntity(updateStore.number.toString(), "storeNumber")
         }

         argsToUpdate.add(store)
      }

      if (updateDepartment != existingDepartment.second) {
         argsToUpdate.add(existingDepartment.first.copy(value = updateDepartment.code))
      } else {
         argsToUpdate.add(existingDepartment.first)
      }

      val scheduleToUpdate = schedule.copy(title = dto.title!!, description = dto.description!!, schedule = dto.schedule!!.name, arguments = argsToUpdate, enabled = dto.enabled!!)

      return Triple(
         scheduleToUpdate,
         stores,
         updateDepartment
      )
   }

   private fun doSharedValidation(dto: AuditScheduleCreateUpdateDataTransferObject, errors: MutableSet<ValidationError>) {
      if (departmentRepository.doesNotExist(dto.department!!.id!!)) {
         errors.add(ValidationError("department.id", NotFound(dto.department.id!!)))
      }

      for ((i, store) in dto.stores.withIndex()) {
         if (storeRepository.doesNotExist(store.id!!)) {
            errors.add(ValidationError("store[$i].id", NotFound(store.id!!)))
         }
      }
   }
}
