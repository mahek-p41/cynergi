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
@Named("WowAccountSummary") // Must match a row in the schedule_command_type_domain
class WowAccountSummaryJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowAccountSummaryJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-account-summary-$fileDate.csv"
      val accountSummaryTempPath = Files.createTempFile("accountSummary", ".csv")

      Files.newBufferedWriter(accountSummaryTempPath).use { writer ->
         val accountSummaryCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         accountSummaryCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DateRented", "DueDate", "PercentOwnership", "Product","Terms","NextPaymentAmount","Address1", "Address2","City", "State", "Zip", "PaymentsRemaining","ProjectedPayoutDate","WeeksRemaining", "MonthsRemaining", "PastDue", "OverdueAmount","ClubMember","ClubNumber","ClubFee","AutoPay", "PaymentTerms")

         wowRepository.findAccountSummary(company).forEach { accountSummary ->
            accountSummaryCsv.printRecord(
               accountSummary.storeNumber,
               accountSummary.customerNumber,
               accountSummary.firstName ?: EMPTY,
               accountSummary.lastName ?: EMPTY,
               accountSummary.email ?: EMPTY,
               accountSummary.agreementNumber,
               accountSummary.dateRented ?: EMPTY,
               accountSummary.dueDate ?: EMPTY,
               accountSummary.percentOwnership,
               accountSummary.product ?: EMPTY,
               accountSummary.terms,
               accountSummary.nextPaymentAmount,
               accountSummary.address1 ?: EMPTY,
               accountSummary.address2 ?: EMPTY,
               accountSummary.city ?: EMPTY,
               accountSummary.state ?: EMPTY,
               accountSummary.zip ?: EMPTY,
               accountSummary.paymentsRemaining,
               accountSummary.projectedPayoutDate ?: EMPTY,
               accountSummary.weeksRemaining,
               accountSummary.monthsRemaining,
               accountSummary.pastDue ?: EMPTY,
               accountSummary.overdueAmount ?: EMPTY,
               accountSummary.clubMember ?: EMPTY,
               accountSummary.clubNumber ?: EMPTY,
               accountSummary.clubFee,
               accountSummary.autopay ?: EMPTY,
               accountSummary.paymentTerms ?: EMPTY
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(accountSummaryTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Account summary upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(accountSummaryTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(accountSummaryTempPath)

      return WowJobResult("Wow Account Summary", null, lineCount)
   }
}
