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
@Named("WowFuturePayout") // Must match a row in the schedule_command_type_domain
class WowFuturePayoutJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowFuturePayoutJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-future-payouts-$fileDate.csv"
      val wowFuturePayoutsTempPath = Files.createTempFile("wowFuturePayouts", ".csv")

      Files.newBufferedWriter(wowFuturePayoutsTempPath).use { writer ->
         val wowFuturePayoutCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         wowFuturePayoutCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DateRented", "DueDate", "PercentOwnership", "Product","Terms","NextPaymentAmount","Address1", "Address2","City", "State", "Zip", "PaymentsRemaining","ProjectedPayoutDate","WeeksRemaining", "MonthsRemaining", "PastDue", "OverdueAmount","ClubMember","ClubNumber","ClubFee","AutoPay", "ActiveAgreement", "PaymentTerms")

         wowRepository.findWowFuturePayout(company).forEach { wowFuturePayout ->
            wowFuturePayoutCsv.printRecord(
               wowFuturePayout.storeNumber,
               wowFuturePayout.customerNumber,
               wowFuturePayout.firstName ?: EMPTY,
               wowFuturePayout.lastName ?: EMPTY,
               wowFuturePayout.email ?: EMPTY,
               wowFuturePayout.agreementNumber,
               wowFuturePayout.dateRented ?: EMPTY,
               wowFuturePayout.dueDate ?: EMPTY,
               wowFuturePayout.percentOwnership ?: EMPTY,
               wowFuturePayout.product ?: EMPTY,
               wowFuturePayout.terms ?: EMPTY,
               wowFuturePayout.nextPaymentAmount ?: EMPTY,
               wowFuturePayout.address1 ?: EMPTY,
               wowFuturePayout.address2 ?: EMPTY,
               wowFuturePayout.city ?: EMPTY,
               wowFuturePayout.state ?: EMPTY,
               wowFuturePayout.zip ?: EMPTY,
               wowFuturePayout.paymentsRemaining ?: EMPTY,
               wowFuturePayout.projectedPayoutDate ?: EMPTY,
               wowFuturePayout.weeksRemaining ?:EMPTY,
               wowFuturePayout.monthsRemaining ?: EMPTY,
               wowFuturePayout.pastDue ?: EMPTY,
               wowFuturePayout.overdueAmount ?: EMPTY,
               wowFuturePayout.clubMember ?: EMPTY,
               wowFuturePayout.clubNumber ?: EMPTY,
               wowFuturePayout.clubFee ?: EMPTY,
               wowFuturePayout.autopay ?: EMPTY,
               wowFuturePayout.actveAgreement ?: EMPTY,
               wowFuturePayout.paymentTerms ?: EMPTY
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(wowFuturePayoutsTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Wow future payout upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(wowFuturePayoutsTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(wowFuturePayoutsTempPath)

      return WowJobResult("Wow Future Payouts Next 30 Days", null, lineCount)
   }
}
