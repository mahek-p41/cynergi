package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.wow.infrastructure.WowRepository
import com.cynergisuite.middleware.wow.schedule.spi.WowScheduledJob
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import com.cynergisuite.middleware.ssh.SftpClientService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.OffsetDateTime

@Singleton
@Named("WowNewRentals") // Must match a row in the schedule_command_type_domain
class WowNewRentalJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowNewRentalJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-new-rentals-$fileDate.csv"
      val newRentalsTempPath = Files.createTempFile("newRentals", ".csv")

      Files.newBufferedWriter(newRentalsTempPath).use { writer ->
         val newRentalsCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         newRentalsCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DateRented", "DueDate", "PercentOwnership", "Product","Terms","NextPaymentAmount","Address1", "Address2","City", "State", "Zip", "PaymentsRemaining","ProjectedPayoutDate","WeeksRemaining", "MonthsRemaining", "PastDue", "OverdueAmount","ClubMember","ClubNumber","ClubFee","AutoPay", "ActiveAgreement", "PaymentTerms")

         wowRepository.findnewRentals(company).forEach { newRentals ->
            newRentalsCsv.printRecord(
               newRentals.storeNumber,
               newRentals.customerNumber,
               newRentals.firstName ?: EMPTY,
               newRentals.lastName ?: EMPTY,
               newRentals.email ?: EMPTY,
               newRentals.agreementNumber ?: EMPTY,
               newRentals.dateRented ?: EMPTY,
               newRentals.dueDate ?: EMPTY,
               newRentals.percentOwnership ?: EMPTY,
               newRentals.product ?: EMPTY,
               newRentals.terms ?: EMPTY,
               newRentals.nextPaymentAmount ?: EMPTY,
               newRentals.address1 ?: EMPTY,
               newRentals.address2 ?: EMPTY,
               newRentals.city ?: EMPTY,
               newRentals.state ?: EMPTY,
               newRentals.zip ?: EMPTY,
               newRentals.paymentsRemaining ?: EMPTY,
               newRentals.projectedPayoutDate ?: EMPTY,
               newRentals.weeksRemaining ?:EMPTY,
               newRentals.monthsRemaining ?: EMPTY,
               newRentals.pastDue ?: EMPTY,
               newRentals.overdueAmount ?: EMPTY,
               newRentals.clubMember ?: EMPTY,
               newRentals.clubNumber ?: EMPTY,
               newRentals.clubFee ?: EMPTY,
               newRentals.autopay ?: EMPTY,
               newRentals.actveAgreement ?: EMPTY,
               newRentals.paymentTerms ?: EMPTY
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(newRentalsTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("New rentals upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(newRentalsTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(newRentalsTempPath)

      return WowJobResult("Wow New Rentals Last 30 Days", null, lineCount)
   }
}
