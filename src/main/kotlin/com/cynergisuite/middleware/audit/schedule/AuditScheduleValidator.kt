package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.AUDIT_STATUS
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.WEEKLY
import com.cynergisuite.middleware.schedule.type.infrastructure.ScheduleTypeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import java.util.Locale
import javax.inject.Singleton

@Singleton
class AuditScheduleValidator(
   private val departmentRepository: DepartmentRepository,
   private val scheduleRepository: ScheduleRepository,
   private val scheduleTypeRepository: ScheduleTypeRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {

   fun validateCreate(dto: AuditScheduleCreateUpdateDataTransferObject, user: User, locale: Locale): Pair<ScheduleEntity, List<StoreEntity>> {
      doValidation {
         errors -> doSharedValidation(dto, errors)
      }

      val stores = mutableListOf<StoreEntity>()
      val arguments = mutableSetOf(
         ScheduleArgumentEntity(
            locale.toLanguageTag(),
            "locale"
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

      arguments.add(
         ScheduleArgumentEntity(
            user.myEmployeeNumber().toString(),
            "employeeNumber"
         )
      )

      return Pair(
         ScheduleEntity(
            title = dto.title!!,
            description = dto.description,
            schedule = dto.schedule!!.name,
            command = AUDIT_STATUS,
            type = WEEKLY,
            arguments = arguments,
            enabled = dto.enabled!!
         ),
         stores
      )
   }

   fun validateUpdate(dto: AuditScheduleCreateUpdateDataTransferObject, user: User, locale: Locale): Pair<ScheduleEntity, List<StoreEntity>> {
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
      val existingEmployee = schedule.arguments.firstOrNull { it.description == "employeeNumber" }
      val existingLocale = schedule.arguments.firstOrNull { it.description == "locale" }
      val existingStores: List<Pair<ScheduleArgumentEntity, StoreEntity>> = schedule.arguments.asSequence()
         .filter { it.description == "storeNumber" }
         .map { it to storeRepository.findOne(it.value.toInt(), user.myDataset())!! }
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

      if (existingEmployee == null) {
         argsToUpdate.add(ScheduleArgumentEntity(user.myEmployeeNumber().toString(), "employeeNumber"))
      } else if (existingEmployee.value != user.myEmployeeNumber().toString()) {
         argsToUpdate.add(existingEmployee.copy(value = user.myEmployeeNumber().toString()))
      } else {
         argsToUpdate.add(existingEmployee)
      }

      if (existingLocale == null) { // handle the case where a schedule might have been created without a locale
         argsToUpdate.add(ScheduleArgumentEntity(locale.toLanguageTag(), "locale"))
      } else if (existingLocale.value != locale.toLanguageTag()) {
         argsToUpdate.add(existingLocale.copy(value = locale.toLanguageTag()))
      } else {
         argsToUpdate.add(existingLocale)
      }

      val scheduleToUpdate = schedule.copy(title = dto.title!!, description = dto.description!!, schedule = dto.schedule!!.name, arguments = argsToUpdate, enabled = dto.enabled!!)

      return scheduleToUpdate to stores
   }

   private fun doSharedValidation(dto: AuditScheduleCreateUpdateDataTransferObject, errors: MutableSet<ValidationError>) {
      for ((i, store) in dto.stores.withIndex()) {
         if (storeRepository.doesNotExist(store.id!!)) {
            errors.add(ValidationError("store[$i].id", NotFound(store.id!!)))
         }
      }
   }
}
