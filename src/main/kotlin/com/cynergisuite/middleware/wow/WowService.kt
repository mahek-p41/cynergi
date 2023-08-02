package com.cynergisuite.middleware.wow

import com.cynergisuite.middleware.area.AreaEntity
import com.cynergisuite.middleware.area.AreaTypeEntity
import com.cynergisuite.middleware.area.WowUpload
import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.area.toAreaTypeEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.argument.infrastructure.ScheduleArgumentRepository
import com.cynergisuite.middleware.schedule.command.ScheduleCommandType
import com.cynergisuite.middleware.schedule.command.WowAccountSummary
import com.cynergisuite.middleware.schedule.command.WowActiveInventory
import com.cynergisuite.middleware.schedule.command.WowAllRtoAgreements
import com.cynergisuite.middleware.schedule.command.WowAtRisk
import com.cynergisuite.middleware.schedule.command.WowBirthday
import com.cynergisuite.middleware.schedule.command.WowCollection
import com.cynergisuite.middleware.schedule.command.WowFinalPayment
import com.cynergisuite.middleware.schedule.command.WowFuturePayouts
import com.cynergisuite.middleware.schedule.command.WowLostCustomer
import com.cynergisuite.middleware.schedule.command.WowNewRentals
import com.cynergisuite.middleware.schedule.command.WowPayouts
import com.cynergisuite.middleware.schedule.command.WowReturns
import com.cynergisuite.middleware.schedule.command.WowSingleAgreement
import com.cynergisuite.middleware.schedule.command.toEntity
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.BeginningOfMonth
import com.cynergisuite.middleware.schedule.type.Daily
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.schedule.type.toEntity
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.transaction.Transactional

@Singleton
class WowService @Inject constructor(
   private val areaRepository: AreaRepository,
   private val scheduleRepository: ScheduleRepository,
   private val scheduleArgumentRepository: ScheduleArgumentRepository,
) {
   private val logger: Logger = LoggerFactory.getLogger(WowService::class.java)

   @Transactional // I want a rollback if any of these inserts fail
   fun enableFor(company: CompanyEntity, credentials: SftpClientCredentials): List<ScheduleEntity> {
      val wowEntity: AreaTypeEntity = WowUpload.toAreaTypeEntity()

      return if (!areaRepository.existsByCompanyAndAreaType(company, wowEntity)) {
         logger.info("Enabling Wow for company {}", company.id)

         areaRepository.save(AreaEntity(areaType = wowEntity, company = company))

         listOf(
            saveSchedule("Wow Active Inventory", "DAILY", WowActiveInventory, Daily, company, credentials),
            saveSchedule("Wow Account Summary", "DAILY", WowAccountSummary, Daily, company, credentials),
            saveSchedule("Wow Birthdays", "BEGINNING", WowBirthday, BeginningOfMonth, company, credentials),
            saveSchedule("Wow Collections", "DAILY", WowCollection, Daily, company, credentials),
            saveSchedule("Wow Final Payments", "DAILY", WowFinalPayment, Daily, company, credentials),
            saveSchedule("Wow Single Agreements", "BEGINNING", WowSingleAgreement, BeginningOfMonth, company, credentials),
            saveSchedule("Wow All Rto Agreements", "BEGINNING", WowAllRtoAgreements, BeginningOfMonth, company, credentials),
            saveSchedule("Wow New Rentals Last 30 Days", "DAILY", WowNewRentals, Daily, company, credentials),
            saveSchedule("Wow Returns Last 120 Days", "DAILY", WowReturns, Daily, company, credentials),
            saveSchedule("Wow Lost Customer Last 9 Months", "DAILY", WowLostCustomer, Daily, company, credentials),
            saveSchedule("Wow Payouts Last 120 Days", "DAILY", WowPayouts, Daily, company, credentials),
            saveSchedule("Wow At Risk Anyone Overdue", "DAILY", WowAtRisk, Daily, company, credentials),
            saveSchedule("Wow Future Payouts Next 30 Days", "DAILY", WowFuturePayouts, Daily, company, credentials),
         )
      } else {
         emptyList()
      }
   }

   @Transactional
   fun disableFor(company: CompanyEntity) {
      val area = areaRepository.findByCompanyAndAreaType(company, WowUpload.toAreaTypeEntity())

      if (area != null) {
         areaRepository.delete(area)
         scheduleRepository.deleteByTypesForCompanyCascade(listOf(WowActiveInventory, WowAccountSummary, WowBirthday, WowCollection, WowFinalPayment, WowSingleAgreement), company)
      }
   }

   private fun saveSchedule(title: String, schedule: String, command: ScheduleCommandType, type: ScheduleType, company: CompanyEntity, credentials: SftpClientCredentials): ScheduleEntity {
      val scheduled = scheduleRepository.insert(
         ScheduleEntity(
            title = title,
            description = "$title ${company.datasetCode}",
            schedule = schedule, // this isn't used by this process
            command = command.toEntity(),
            type = type.toEntity(),
            company = company,
         )
      )

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
