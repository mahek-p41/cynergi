package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.AuditService
import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.authentication.user.EmployeeUser
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.notification.NotificationService
import com.cynergisuite.middleware.notification.NotificationValueObject
import com.cynergisuite.middleware.notification.STORE
import com.cynergisuite.middleware.schedule.DailySchedule
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.ScheduleName
import com.cynergisuite.middleware.schedule.ScheduleProcessingException
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.infrastructure.SchedulePageRequest
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.store.StoreValueObject
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.validation.Validated
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@ScheduleName("AuditSchedule")
class AuditScheduleService @Inject constructor(
   private val auditService: AuditService,
   private val auditScheduleValidator: AuditScheduleValidator,
   private val companyRepository: CompanyRepository,
   private val employeeRepository: EmployeeRepository,
   private val scheduleRepository: ScheduleRepository,
   private val storeRepository: StoreRepository,
   private val notificationService: NotificationService
) : DailySchedule {

   fun fetchById(id: Long, company: Company): AuditScheduleDataTransferObject? {
      val schedule = scheduleRepository.findOne(id)

      return if (schedule != null) {
         buildAuditScheduleValueObjectFromSchedule(schedule, company)
      } else {
         null
      }
   }

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest, company: Company): Page<AuditScheduleDataTransferObject> {
      val repoPage = scheduleRepository.findAll(SchedulePageRequest(pageRequest, "AuditSchedule")) // find all schedules that are of a command AuditSchedule

      return repoPage.toPage { buildAuditScheduleValueObjectFromSchedule(it, company) }
   }

   @Validated
   fun create(@Valid dto: AuditScheduleCreateUpdateDataTransferObject, employee: User, locale: Locale): AuditScheduleDataTransferObject {
      val (schedule, stores) = auditScheduleValidator.validateCreate(dto, employee, locale)
      val inserted = scheduleRepository.insert(schedule)

      return AuditScheduleDataTransferObject(
         id = inserted.id,
         title = inserted.title,
         description = inserted.description,
         schedule = DayOfWeek.valueOf(inserted.schedule),
         stores = stores.map { StoreValueObject(it) },
         enabled = inserted.enabled
      )
   }

   @Validated
   fun update(@Valid dto: AuditScheduleCreateUpdateDataTransferObject, user: User, locale: Locale): AuditScheduleDataTransferObject {
      val (schedule, stores) = auditScheduleValidator.validateUpdate(dto, user, locale)
      val updated = scheduleRepository.update(schedule)

      return AuditScheduleDataTransferObject(
         id = updated.id,
         title = updated.title,
         description = updated.description,
         schedule = DayOfWeek.valueOf(schedule.schedule),
         stores = stores.map { StoreValueObject(it) },
         enabled = updated.enabled
      )
   }

   private fun buildAuditScheduleValueObjectFromSchedule(schedule: ScheduleEntity, company: Company): AuditScheduleDataTransferObject {
      val stores = mutableListOf<StoreValueObject>()

      for (arg: ScheduleArgumentEntity in schedule.arguments) {
         if (arg.description == "storeNumber") {
            val store = storeRepository.findOne(arg.value.toInt(), company)!!

            stores.add(StoreValueObject(store))
         }
      }

      return AuditScheduleDataTransferObject(
         id = schedule.id,
         title = schedule.title,
         description = schedule.description,
         schedule = DayOfWeek.valueOf(schedule.schedule),
         stores = stores,
         enabled = schedule.enabled
      )
   }

   @Throws(ScheduleProcessingException::class)
   override fun processDaily(schedule: ScheduleEntity) : AuditScheduleResult {
      val notifications = mutableListOf<NotificationValueObject>()
      val audits = mutableListOf<AuditValueObject>()
      val company = schedule.arguments.firstOrNull { it.description == "companyId" }?.value?.let { companyRepository.findOne(it.toLong()) } ?: throw ScheduleProcessingException("Unable to determine company for schedule")
      val locale = schedule.arguments.asSequence()
         .filter { it.description == "locale" }
         .map { Locale.forLanguageTag(it.value) }
         .firstOrNull() ?: Locale.getDefault()
      val employeeType = schedule.arguments.asSequence()
         .filter { it.description == "employeeType" }
         .map { it.value }
         .firstOrNull() ?: "sysz" // TODO remove this when employees are all managed by cynergidb
      val employee = schedule.arguments.asSequence()
         .filter { it.description == "employeeNumber" }
         .filterNotNull()
         .map { it.value.toInt() }
         .map { employeeRepository.findOne(number = it, employeeType = employeeType, company = company) }
         .firstOrNull() ?: throw ScheduleProcessingException("Unable to find employee who scheduled audit")

      for (arg: ScheduleArgumentEntity in schedule.arguments) {
         if (arg.description == "storeNumber") {
            val store = storeRepository.findOne(arg.value.toInt(), company)!!
            val employeeUser = EmployeeUser(
               id = employee.id!!,
               type = employee.type,
               number = employee.number,
               company = employee.company,
               department = employee.department,
               location = store,
               passCode = employee.passCode!!
            )
            val oneNote = NotificationValueObject(
               startDate = LocalDate.now(),
               dateCreated = null,
               expirationDate = LocalDate.now().plusDays(7),
               company = company.id.toString(),
               message = schedule.description!!,
               sendingEmployee = employee.number.toString(),
               notificationType = STORE.value
            )

            val audit = auditService.findOrCreate(store, employeeUser, locale)
            val notification = notificationService.create(oneNote)

            audits.add(audit)
            notifications.add(notification)
         }
      }

      return AuditScheduleResult(audits, notifications)
   }
}
