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
@Named("WowLostCustomer") // Must match a row in the schedule_command_type_domain
class WowLostCustomerJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowLostCustomerJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-lost-customer-$fileDate.csv"
      val lostCustomerTempPath = Files.createTempFile("lostCustomer", ".csv")

      Files.newBufferedWriter(lostCustomerTempPath).use { writer ->
         val lostCustomerCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         lostCustomerCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DateRented", "DueDate", "PercentOwnership", "Product","Terms","NextPaymentAmount","Address1", "Address2","City", "State", "Zip", "PaymentsRemaining","ProjectedPayoutDate","WeeksRemaining", "MonthsRemaining", "PastDue", "OverdueAmount","ClubMember","ClubNumber","ClubFee","AutoPay", "ActiveAgreement","PaymentTerms","DateClosed","ClosedReason")

         wowRepository.findlostCustomer(company).forEach { lostCustomer ->
            lostCustomerCsv.printRecord(
               lostCustomer.storeNumber,
               lostCustomer.customerNumber,
               lostCustomer.firstName ?: EMPTY,
               lostCustomer.lastName ?: EMPTY,
               lostCustomer.email ?: EMPTY,
               lostCustomer.agreementNumber ?: EMPTY,
               lostCustomer.dateRented ?: EMPTY,
               lostCustomer.dueDate ?: EMPTY,
               lostCustomer.percentOwnership ?: EMPTY,
               lostCustomer.product ?: EMPTY,
               lostCustomer.terms ?: EMPTY,
               lostCustomer.nextPaymentAmount ?: EMPTY,
               lostCustomer.address1 ?: EMPTY,
               lostCustomer.address2 ?: EMPTY,
               lostCustomer.city ?: EMPTY,
               lostCustomer.state ?: EMPTY,
               lostCustomer.zip ?: EMPTY,
               lostCustomer.paymentsRemaining ?: EMPTY,
               lostCustomer.projectedPayoutDate ?: EMPTY,
               lostCustomer.weeksRemaining ?:EMPTY,
               lostCustomer.monthsRemaining ?: EMPTY,
               lostCustomer.pastDue ?: EMPTY,
               lostCustomer.overdueAmount ?: EMPTY,
               lostCustomer.clubMember ?: EMPTY,
               lostCustomer.clubNumber ?: EMPTY,
               lostCustomer.clubFee ?: EMPTY,
               lostCustomer.autopay ?: EMPTY,
               lostCustomer.actveAgreement ?: EMPTY,
               lostCustomer.paymentTerms ?: EMPTY,
               lostCustomer.dateClosed ?: EMPTY,
               lostCustomer.closedReason?: EMPTY
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(lostCustomerTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("lost customer upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(lostCustomerTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(lostCustomerTempPath)

      return WowJobResult("Wow Lost Customer Last 9 Months", null, lineCount)
   }
}
