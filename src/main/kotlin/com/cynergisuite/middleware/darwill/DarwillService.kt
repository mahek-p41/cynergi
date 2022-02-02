package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.area.AreaEntity
import com.cynergisuite.middleware.area.DarwillUpload
import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.argument.infrastructure.ScheduleArgumentRepository
import com.cynergisuite.middleware.schedule.command.DarwillActiveCustomer
import com.cynergisuite.middleware.schedule.command.DarwillBirthday
import com.cynergisuite.middleware.schedule.command.DarwillCollection
import com.cynergisuite.middleware.schedule.command.DarwillInactiveCustomer
import com.cynergisuite.middleware.schedule.command.DarwillLastWeeksDelivery
import com.cynergisuite.middleware.schedule.command.DarwillLastWeeksPayout
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.BeginningOfMonth
import com.cynergisuite.middleware.schedule.type.Daily
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.schedule.type.Weekly
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class DarwillService @Inject constructor(
   private val areaRepository: AreaRepository,
   private val scheduleRepository: ScheduleRepository,
   private val scheduleArgumentRepository: ScheduleArgumentRepository,
) {
   private val logger: Logger = LoggerFactory.getLogger(DarwillService::class.java)

   @Transactional // I want a rollback if any of these inserts fail
   fun enableFor(company: CompanyEntity, credentials: SftpClientCredentials): List<ScheduleEntity> {
      return if (!areaRepository.existsByCompanyAndAreaType(company, DarwillUpload)) {
         logger.info("Enabling Darwill for company {}", company.id)

         areaRepository.save(AreaEntity(areaType = DarwillUpload, company = company))

         listOf(
            saveSchedule("Darwill Inactive Customer", "BEGINNING", DarwillInactiveCustomer, BeginningOfMonth, company, credentials),
            saveSchedule("Darwill Active Customer", "SUNDAY", DarwillActiveCustomer, Weekly, company, credentials),
            saveSchedule("Darwill Birthdays", "BEGINNING", DarwillBirthday, BeginningOfMonth, company, credentials),
            saveSchedule("Darwill Collections", "DAILY", DarwillCollection, Daily, company, credentials),
            saveSchedule("Darwill Last Weeks Deliveries", "SUNDAY", DarwillLastWeeksDelivery, Weekly, company, credentials),
            saveSchedule("Darwill Last Weeks Payouts", "SUNDAY", DarwillLastWeeksPayout, Weekly, company, credentials),
         )
      } else {
         emptyList()
      }
   }

   @Transactional
   fun disableFor(company: CompanyEntity) {
      val area = areaRepository.findByCompanyAndAreaType(company, DarwillUpload)

      if (area != null) {
         areaRepository.delete(area)
         scheduleRepository.deleteByTypesForCompanyCascade(listOf(DarwillInactiveCustomer, DarwillActiveCustomer, DarwillBirthday, DarwillCollection, DarwillLastWeeksDelivery, DarwillLastWeeksPayout), company)
      }
   }

   private fun saveSchedule(title: String, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleType, company: CompanyEntity, credentials: SftpClientCredentials): ScheduleEntity {
      val scheduled = scheduleRepository.insert(ScheduleEntity(
         title = title,
         description = "$title ${company.datasetCode}",
         schedule = schedule, // this isn't used by this process
         command = command,
         type = type,
         company = company,
      ))

      val args = listOf(
         scheduleArgumentRepository.insert(scheduled, ScheduleArgumentEntity(credentials.host, "sftpHost")),
         scheduleArgumentRepository.insert(scheduled, ScheduleArgumentEntity(credentials.port.toString(), "sftpPort")),
         scheduleArgumentRepository.insert(scheduled, ScheduleArgumentEntity(credentials.username, "sftpUsername")),
         scheduleArgumentRepository.insert(scheduled, ScheduleArgumentEntity(credentials.password, "sftpPassword", true)),
      )

      scheduled.arguments.addAll(args)

      return scheduled
   }
}
