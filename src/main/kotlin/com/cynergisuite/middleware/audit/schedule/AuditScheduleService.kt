package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.DepartmentValueObject
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.notification.Notification
import com.cynergisuite.middleware.notification.NotificationService
import com.cynergisuite.middleware.notification.NotificationValueObject
import com.cynergisuite.middleware.notification.infrastructure.NotificationRepository
import com.cynergisuite.middleware.notification.infrastructure.NotificationTypeDomainRepository
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.infrastructure.SchedulePageRequest
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.store.StoreValueObject
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.validation.Validated
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditScheduleService @Inject constructor(
   private val auditScheduleValidator: AuditScheduleValidator,
   private val departmentRepository: DepartmentRepository,
   private val scheduleRepository: ScheduleRepository,
   private val storeRepository: StoreRepository,
   private val auditRepository: AuditRepository,
   private val notificationRepository: NotificationRepository,
   private val notificationService: NotificationService,
   private val companyRepository: CompanyRepository,
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository

) {

   fun fetchById(id: Long): AuditScheduleDataTransferObject? {
      val schedule = scheduleRepository.findOne(id)

      return if (schedule != null) {
         buildAuditScheduleValueObjectFromSchedule(schedule)
      } else {
         null
      }
   }

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest): Page<AuditScheduleDataTransferObject> {
      val repoPage = scheduleRepository.fetchAll(SchedulePageRequest(pageRequest, "AuditSchedule")) // find all schedules that are of a command AuditSchedule

      return repoPage.toPage { buildAuditScheduleValueObjectFromSchedule(it) }
   }

   @Validated
   fun create(@Valid dto: AuditScheduleCreateUpdateDataTransferObject): AuditScheduleDataTransferObject {
      val (schedule, stores, department) = auditScheduleValidator.validateCreate(dto)
      val inserted = scheduleRepository.insert(schedule)

      return AuditScheduleDataTransferObject(
         id = inserted.id,
         title = inserted.title,
         description = inserted.description,
         schedule = DayOfWeek.valueOf(inserted.schedule),
         stores = stores.map { StoreValueObject(it) },
         department = DepartmentValueObject(department),
         enabled = inserted.enabled
      )
   }

   @Validated
   fun update(@Valid dto: AuditScheduleCreateUpdateDataTransferObject): AuditScheduleDataTransferObject {
      val (schedule, stores, department) = auditScheduleValidator.validateUpdate(dto)
      val updated = scheduleRepository.update(schedule)

      return AuditScheduleDataTransferObject(
         id = updated.id,
         title = updated.title,
         description = updated.description,
         schedule = DayOfWeek.valueOf(schedule.schedule),
         stores = stores.map { StoreValueObject(it) },
         department = DepartmentValueObject(department),
         enabled = updated.enabled
      )
   }

   private fun buildAuditScheduleValueObjectFromSchedule(schedule: ScheduleEntity): AuditScheduleDataTransferObject {
      val stores = mutableListOf<StoreValueObject>()
      var department: DepartmentValueObject? = null

      for (arg: ScheduleArgumentEntity in schedule.arguments) {
         if (arg.description == "storeNumber") {
            val store = storeRepository.findOneByNumber(arg.value.toInt())!!

            stores.add(StoreValueObject(store))
         } else if (arg.description == "department") {
            val dept = departmentRepository.findOneByCode(arg.value)!!

            department = DepartmentValueObject(dept)
         }
      }

      return AuditScheduleDataTransferObject(
         id = schedule.id,
         title = schedule.title,
         description = schedule.description,
         schedule = DayOfWeek.valueOf(schedule.schedule),
         stores = stores,
         department = department,
         enabled = schedule.enabled
      )
   }

   private fun createNotificationAndAudit(schedule: ScheduleEntity) {
      val stores = mutableListOf<StoreValueObject>()
      val notificationDomainType = notificationTypeDomainRepository.findOne("S")!!

      for (arg: ScheduleArgumentEntity in schedule.arguments) {
         if (arg.description == "storeNumber") {
            val store = storeRepository.findOneByNumber(arg.value.toInt())!!

            stores.add(StoreValueObject(store))
            var companyName = companyRepository.findCompanyByStore(store)?.name
            val xx = companyRepository.findCompanyByStore(store)
            val oneNote = Notification(startDate = LocalDate.now(),
                                       expirationDate = LocalDate.now().plusDays(7),
                                       message = schedule.description!!,
                                       sendingEmployee = "todo",
                                       company = companyName!!,
                                       notificationDomainType = notificationDomainType)

            xxx = notificationRepository.insert(note)
         }
      }

   }
}
