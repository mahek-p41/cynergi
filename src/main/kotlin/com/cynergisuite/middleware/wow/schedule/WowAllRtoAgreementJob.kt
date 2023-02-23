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
@Named("WowAllRtoAgreements") // Must match a row in the schedule_command_type_domain
class WowAllRtoAgreementJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowAllRtoAgreementJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-all-rto-agreements-$fileDate.csv"
      val allRtoAgreementsTempPath = Files.createTempFile("allRtoAgreements", ".csv")

      Files.newBufferedWriter(allRtoAgreementsTempPath).use { writer ->
         val allRtoAgreementsCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         allRtoAgreementsCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DateRented", "DueDate", "PercentOwnership", "Product","Terms","NextPaymentAmount","Address1", "Address2","City", "State", "Zip", "PaymentsRemaining","ProjectedPayoutDate","WeeksRemaining", "MonthsRemaining", "PastDue", "OverdueAmount","ClubMember","ClubNumber","ClubFee","AutoPay", "ActiveAgreement", "PaymentTerms","DateClosed","ClosedReason")

         wowRepository.findAllRtoAgreements(company).forEach { allRtoAgreements ->
            allRtoAgreementsCsv.printRecord(
               allRtoAgreements.storeNumber,
               allRtoAgreements.customerNumber,
               allRtoAgreements.firstName ?: EMPTY,
               allRtoAgreements.lastName ?: EMPTY,
               allRtoAgreements.email ?: EMPTY,
               allRtoAgreements.agreementNumber,
               allRtoAgreements.dateRented ?: EMPTY,
               allRtoAgreements.dueDate ?: EMPTY,
               allRtoAgreements.percentOwnership,
               allRtoAgreements.product ?: EMPTY,
               allRtoAgreements.terms,
               allRtoAgreements.nextPaymentAmount,
               allRtoAgreements.address1 ?: EMPTY,
               allRtoAgreements.address2 ?: EMPTY,
               allRtoAgreements.city ?: EMPTY,
               allRtoAgreements.state ?: EMPTY,
               allRtoAgreements.zip ?: EMPTY,
               allRtoAgreements.paymentsRemaining,
               allRtoAgreements.projectedPayoutDate ?: EMPTY,
               allRtoAgreements.weeksRemaining,
               allRtoAgreements.monthsRemaining,
               allRtoAgreements.pastDue ?: EMPTY,
               allRtoAgreements.overdueAmount ?: EMPTY,
               allRtoAgreements.clubMember ?: EMPTY,
               allRtoAgreements.clubNumber ?: EMPTY,
               allRtoAgreements.clubFee,
               allRtoAgreements.autopay ?: EMPTY,
               allRtoAgreements.actveAgreement ?: EMPTY,
               allRtoAgreements.paymentTerms ?: EMPTY,
               allRtoAgreements.dateClosed ?: EMPTY,
               allRtoAgreements.closedReason
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(allRtoAgreementsTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("All Rto Agreements upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(allRtoAgreementsTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(allRtoAgreementsTempPath)

      return WowJobResult("Wow All Rto Agreements", null, lineCount)
   }
}
