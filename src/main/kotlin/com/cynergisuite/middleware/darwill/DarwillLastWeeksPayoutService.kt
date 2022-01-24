package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.darwill.infrastructure.DarwillRepository
import com.cynergisuite.middleware.schedule.JobResult
import com.cynergisuite.middleware.schedule.OnceDailyJob
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import com.cynergisuite.middleware.ssh.SftpClientService
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton

@Singleton
@Named("DarwillLastWeeksPayout")
class DarwillLastWeeksPayoutService @Inject constructor(
   areaService: AreaService,
   private val darwillRepository: DarwillRepository,
   private val sftpClientService: SftpClientService,
) : OnceDailyJob, DarwillScheduledService<DayOfWeek>(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(DarwillLastWeeksPayoutService::class.java)

   override fun shouldProcess(time: DayOfWeek): Boolean = time == MONDAY

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: DayOfWeek): DarwillJobResult {
      val pushedFileName = "${company.clientCode}-last-week-payouts.csv"
      val lastWeeksPayoutTempPath = Files.createTempFile("lastWeeksPayout", ".csv")

      Files.newBufferedWriter(lastWeeksPayoutTempPath).use { writer ->
         val lastWeeksPayoutCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         lastWeeksPayoutCsv.printRecord("StoreId", "PeopleID", "UniqueId", "FirstName", "LastName", "Address1", "Address2", "City", "State", "Zip", "CellPhone", "HomePhone", "Email", "AgreementId", "FinalStatus", "PayoutDate")

         darwillRepository.findLastWeeksPayouts(company).forEach { lastWeeksPayout ->
            lastWeeksPayoutCsv.printRecord(
               lastWeeksPayout.storeId,
               lastWeeksPayout.peopleId,
               lastWeeksPayout.uniqueId,
               lastWeeksPayout.firstName,
               lastWeeksPayout.lastName,
               lastWeeksPayout.address1,
               lastWeeksPayout.address2,
               lastWeeksPayout.city,
               lastWeeksPayout.state,
               lastWeeksPayout.zip,
               lastWeeksPayout.cellPhoneNumber,
               lastWeeksPayout.homePhoneNumber,
               lastWeeksPayout.email,
               lastWeeksPayout.agreementId,
               lastWeeksPayout.finalStatus,
               lastWeeksPayout.payoutDate
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(lastWeeksPayoutTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Last weeks payout upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(lastWeeksPayoutTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(lastWeeksPayoutTempPath)

      return DarwillJobResult("Darwill Last Weeks Payouts", null, lineCount)
   }
}