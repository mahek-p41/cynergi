package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.AUDIT_SCHEDULE
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.WEEKLY
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import java.util.Locale
import javax.inject.Singleton

@Singleton
class AuditScheduleValidator(
   private val scheduleRepository: ScheduleRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {

   fun validateCreate(dto: AuditScheduleCreateUpdateDataTransferObject, user: User, locale: Locale): Pair<ScheduleEntity, List<StoreEntity>> {
      doValidation { errors -> doSharedValidation(dto, user.myCompany(), errors) }

      val stores = mutableListOf<StoreEntity>()
      val arguments = mutableSetOf(
         ScheduleArgumentEntity(
            locale.toLanguageTag(),
            "locale"
         )
      )

      for (storeIn in dto.stores) {
         val store = storeRepository.findOne(storeIn.myId()!!, user.myCompany())!!

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

      arguments.add(
         ScheduleArgumentEntity(
            user.myEmployeeType(),
            "employeeType"
         )
      )

      arguments.add(
         ScheduleArgumentEntity(
            user.myCompany().myId().toString(),
            "companyId"
         )
      )

      return Pair(
         ScheduleEntity(
            title = dto.title!!,
            description = dto.description,
            schedule = dto.schedule!!.name,
            command = AUDIT_SCHEDULE,
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

         doSharedValidation(dto, user.myCompany(), errors)
      }

      val stores = mutableListOf<StoreEntity>()
      val schedule = scheduleRepository.findOne(dto.id!!)!!
      val existingCompany = schedule.arguments.firstOrNull { it.description == "companyId" }
      val existingLocale = schedule.arguments.firstOrNull { it.description == "locale" }
      val existingEmployeeNumber = schedule.arguments.firstOrNull { it.description == "employeeNumber" }
      val existingEmployeeType = schedule.arguments.first { it.description == "employeeType" }
      val existingStores: List<Pair<ScheduleArgumentEntity, StoreEntity>> = schedule.arguments.asSequence()
         .filter { it.description == "storeNumber" }
         .map { it to storeRepository.findOne(it.value.toInt(), user.myCompany())!! }
         .sortedBy { it.second.id }
         .toList()
      val updateStores: List<StoreEntity> = dto.stores.asSequence()
         .map { storeRepository.findOne(it.id!!, user.myCompany())!! }
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

      existingUpdate(existingCompany, "companyId") { user.myCompany().myId()!!.toString() }.also { argsToUpdate.add(it) }
      existingUpdate(existingEmployeeNumber, "employeeNumber") { user.myEmployeeNumber().toString() }.also { argsToUpdate.add(it) }
      existingUpdate(existingEmployeeType, "employeeType") { user.myEmployeeType() }.also { argsToUpdate.add(it) }
      existingUpdate(existingLocale, "locale") { locale.toLanguageTag() }.also { argsToUpdate.add(it) }

      val scheduleToUpdate = schedule.copy(title = dto.title!!, description = dto.description!!, schedule = dto.schedule!!.name, arguments = argsToUpdate, enabled = dto.enabled!!)

      return scheduleToUpdate to stores
   }

   private fun existingUpdate(existing: ScheduleArgumentEntity?, description: String, replaceWith: () -> String): ScheduleArgumentEntity {
      val replacement = replaceWith()

      return when {
         existing == null -> {
            ScheduleArgumentEntity(replaceWith(), description)
         }
         existing.value != replacement -> {
            existing.copy(value = replacement)
         }
         else -> {
            existing
         }
      }
   }

   private fun doSharedValidation(dto: AuditScheduleCreateUpdateDataTransferObject, company: Company, errors: MutableSet<ValidationError>) {
      for ((i, store) in dto.stores.withIndex()) {
         if (storeRepository.doesNotExist(store.id!!, company)) {
            errors.add(ValidationError("store[$i].id", NotFound(store.id!!)))
         }
      }
   }
}
