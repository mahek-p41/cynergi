package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.department.DepartmentValueObject
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.infrastructure.SchedulePageRequest
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.store.StoreValueObject
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.validation.Validated
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditScheduleService @Inject constructor(
   private val auditScheduleValidator: AuditScheduleValidator,
   private val departmentRepository: DepartmentRepository,
   private val scheduleRepository: ScheduleRepository,
   private val storeRepository: StoreRepository
) {

   fun fetchById(id: Long): AuditScheduleValueObject? {
      val schedule = scheduleRepository.findOne(id)

      return if (schedule != null) {
         buildAuditScheduleValueObjectFromSchedule(schedule)
      } else {
         null
      }
   }

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest): Page<AuditScheduleValueObject> {
      val repoPage = scheduleRepository.fetchAll(SchedulePageRequest(pageRequest, "AuditSchedule")) // find all schedules that are of a command AuditSchedule

      return repoPage.toPage(pageRequest) { buildAuditScheduleValueObjectFromSchedule(it) }
   }

   @Validated
   fun create(@Valid auditScheduleDto: AuditScheduleCreateDataTransferObject): AuditScheduleValueObject {
      val (schedule, stores, department) = auditScheduleValidator.validateCreate(auditScheduleDto)
      val inserted = scheduleRepository.insert(schedule)

      return AuditScheduleValueObject(
         id = inserted.id,
         title = inserted.title,
         description = inserted.description,
         schedule = DayOfWeek.valueOf(inserted.schedule),
         stores = stores.map { StoreValueObject(it) },
         departmentAccess = DepartmentValueObject(department)
      )
   }

   private fun buildAuditScheduleValueObjectFromSchedule(schedule: ScheduleEntity): AuditScheduleValueObject {
      val stores = mutableListOf<StoreValueObject>()
      var department: DepartmentValueObject? = null

      for (arg: ScheduleArgumentEntity in schedule.arguments) {
         if (arg.description == "storeNumber") {
            val store = storeRepository.findOneByNumber(arg.value.toInt())!!

            stores.add(StoreValueObject(store))
         } else {
            val dept = departmentRepository.findOneByCode(arg.value)!!

            department = DepartmentValueObject(dept)
         }
      }

      return AuditScheduleValueObject(
         id = schedule.id,
         title = schedule.title,
         description = schedule.description,
         schedule = DayOfWeek.valueOf(schedule.schedule),
         stores = stores,
         departmentAccess = department
      )
   }
}
