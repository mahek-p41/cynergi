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
@Named("WowAtRisk") // Must match a row in the schedule_command_type_domain
class WowAtRiskJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowAtRiskJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-at-risk-$fileDate.csv"
      val atRiskTempPath = Files.createTempFile("atRisk", ".csv")

      Files.newBufferedWriter(atRiskTempPath).use { writer ->
         val atRiskCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         atRiskCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DateRented", "DueDate", "PercentOwnership", "Product","Terms","NextPaymentAmount","Address1", "Address2","City", "State", "Zip", "PaymentsRemaining","ProjectedPayoutDate","WeeksRemaining", "MonthsRemaining", "PastDue", "OverdueAmount","ClubMember","ClubNumber","ClubFee","AutoPay", "ActiveAgreement", "PaymentTerms")

         wowRepository.findAtRisk(company).forEach { atRisk ->
            atRiskCsv.printRecord(
               atRisk.storeNumber,
               atRisk.customerNumber,
               atRisk.firstName ?: EMPTY,
               atRisk.lastName ?: EMPTY,
               atRisk.email ?: EMPTY,
               atRisk.agreementNumber ?: EMPTY,
               atRisk.dateRented ?: EMPTY,
               atRisk.dueDate ?: EMPTY,
               atRisk.percentOwnership ?: EMPTY,
               atRisk.product ?: EMPTY,
               atRisk.terms ?: EMPTY,
               atRisk.nextPaymentAmount ?: EMPTY,
               atRisk.address1 ?: EMPTY,
               atRisk.address2 ?: EMPTY,
               atRisk.city ?: EMPTY,
               atRisk.state ?: EMPTY,
               atRisk.zip ?: EMPTY,
               atRisk.paymentsRemaining ?: EMPTY,
               atRisk.projectedPayoutDate ?: EMPTY,
               atRisk.weeksRemaining ?:EMPTY,
               atRisk.monthsRemaining ?: EMPTY,
               atRisk.pastDue ?: EMPTY,
               atRisk.overdueAmount ?: EMPTY,
               atRisk.clubMember ?: EMPTY,
               atRisk.clubNumber ?: EMPTY,
               atRisk.clubFee ?: EMPTY,
               atRisk.autopay ?: EMPTY,
               atRisk.actveAgreement ?: EMPTY,
               atRisk.paymentTerms ?: EMPTY
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(atRiskTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("New rentals upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(atRiskTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(atRiskTempPath)

      return WowJobResult("Wow At Risk Anyone Overdue", null, lineCount)
   }
}
