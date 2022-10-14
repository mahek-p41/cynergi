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
@Named("WowPayouts") // Must match a row in the schedule_command_type_domain
class WowPayoutJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowPayoutJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-payouts-$fileDate.csv"
      val payoutsTempPath = Files.createTempFile("payouts", ".csv")

      Files.newBufferedWriter(payoutsTempPath).use { writer ->
         val payoutsCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         payoutsCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DateRented", "DueDate", "PercentOwnership", "Product","Terms","NextPaymentAmount","Address1", "Address2","City", "State", "Zip", "PaymentsRemaining","ProjectedPayoutDate","WeeksRemaining", "MonthsRemaining", "PastDue", "OverdueAmount","ClubMember","ClubNumber","ClubFee","AutoPay", "ActiveAgreement","PaymentTerms","DateClosed","ClosedReason")

         wowRepository.findPayouts(company).forEach { payouts ->
            payoutsCsv.printRecord(
               payouts.storeNumber,
               payouts.customerNumber,
               payouts.firstName ?: EMPTY,
               payouts.lastName ?: EMPTY,
               payouts.email ?: EMPTY,
               payouts.agreementNumber ?: EMPTY,
               payouts.dateRented ?: EMPTY,
               payouts.dueDate ?: EMPTY,
               payouts.percentOwnership ?: EMPTY,
               payouts.product ?: EMPTY,
               payouts.terms ?: EMPTY,
               payouts.nextPaymentAmount ?: EMPTY,
               payouts.address1 ?: EMPTY,
               payouts.address2 ?: EMPTY,
               payouts.city ?: EMPTY,
               payouts.state ?: EMPTY,
               payouts.zip ?: EMPTY,
               payouts.paymentsRemaining ?: EMPTY,
               payouts.projectedPayoutDate ?: EMPTY,
               payouts.weeksRemaining ?:EMPTY,
               payouts.monthsRemaining ?: EMPTY,
               payouts.pastDue ?: EMPTY,
               payouts.overdueAmount ?: EMPTY,
               payouts.clubMember ?: EMPTY,
               payouts.clubNumber ?: EMPTY,
               payouts.clubFee ?: EMPTY,
               payouts.autopay ?: EMPTY,
               payouts.actveAgreement ?: EMPTY,
               payouts.paymentTerms ?: EMPTY,
               payouts.dateClosed ?: EMPTY,
               payouts.closedReason?: EMPTY
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(payoutsTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Payouts upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(payoutsTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(payoutsTempPath)

      return WowJobResult("Wow Payouts Last 120 Days", null, lineCount)
   }
}
