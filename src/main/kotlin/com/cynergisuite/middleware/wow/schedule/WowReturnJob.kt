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
@Named("WowReturns") // Must match a row in the schedule_command_type_domain
class WowReturnJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowReturnJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-returns-$fileDate.csv"
      val returnsTempPath = Files.createTempFile("returns", ".csv")

      Files.newBufferedWriter(returnsTempPath).use { writer ->
         val returnsCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         returnsCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DateRented", "DueDate", "PercentOwnership", "Product","Terms","NextPaymentAmount","Address1", "Address2","City", "State", "Zip", "PaymentsRemaining","ProjectedPayoutDate","WeeksRemaining", "MonthsRemaining", "PastDue", "OverdueAmount","ClubMember","ClubNumber","ClubFee","AutoPay", "ActiveAgreement","PaymentTerms","DateClosed","ClosedReason")

         wowRepository.findreturns(company).forEach { returns ->
            returnsCsv.printRecord(
               returns.storeNumber,
               returns.customerNumber,
               returns.firstName ?: EMPTY,
               returns.lastName ?: EMPTY,
               returns.email ?: EMPTY,
               returns.agreementNumber ?: EMPTY,
               returns.dateRented ?: EMPTY,
               returns.dueDate ?: EMPTY,
               returns.percentOwnership ?: EMPTY,
               returns.product ?: EMPTY,
               returns.terms ?: EMPTY,
               returns.nextPaymentAmount ?: EMPTY,
               returns.address1 ?: EMPTY,
               returns.address2 ?: EMPTY,
               returns.city ?: EMPTY,
               returns.state ?: EMPTY,
               returns.zip ?: EMPTY,
               returns.paymentsRemaining ?: EMPTY,
               returns.projectedPayoutDate ?: EMPTY,
               returns.weeksRemaining ?:EMPTY,
               returns.monthsRemaining ?: EMPTY,
               returns.pastDue ?: EMPTY,
               returns.overdueAmount ?: EMPTY,
               returns.clubMember ?: EMPTY,
               returns.clubNumber ?: EMPTY,
               returns.clubFee ?: EMPTY,
               returns.autopay ?: EMPTY,
               returns.actveAgreement ?: EMPTY,
               returns.paymentTerms ?: EMPTY,
               returns.dateClosed ?: EMPTY,
               returns.closedReason?: EMPTY
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(returnsTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("New rentals upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(returnsTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(returnsTempPath)

      return WowJobResult("Wow Returns Last 120 Days", null, lineCount)
   }
}
