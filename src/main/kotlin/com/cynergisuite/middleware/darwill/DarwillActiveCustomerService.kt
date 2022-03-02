package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.darwill.infrastructure.DarwillRepository
import com.cynergisuite.middleware.schedule.OnceDailyJob
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
import java.time.DayOfWeek

@Singleton
@Named("DarwillActiveCustomer")
class DarwillActiveCustomerService @Inject constructor(
   areaService: AreaService,
   private val darwillRepository: DarwillRepository,
   private val sftpClientService: SftpClientService,
) : OnceDailyJob, DarwillScheduledService<DayOfWeek>(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(DarwillActiveCustomerService::class.java)

   override fun shouldProcess(time: DayOfWeek): Boolean = time == DayOfWeek.MONDAY

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: DayOfWeek, fileDate: String): DarwillJobResult {
      val pushedFileName = "${company.clientCode}-active-customers-${fileDate}.csv"
      val activeCustomerTempPath = Files.createTempFile("activeCustomer", ".csv")

      Files.newBufferedWriter(activeCustomerTempPath).use { writer -> // write a csv to the temp file defined above
         val activeCustomerCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         activeCustomerCsv.printRecord("StoreId", "PeopleID", "UniqueId", "FirstName", "LastName", "Address1", "Address2", "City", "State", "Zip", "CellPhone", "HomePhone", "Email", "AgreementId", "PaymentFrequency", "TextOptIn", "OnlineIndicator", "CarePlus", "ProjectPayout", "PaymentsLeftInWeeks", "PastDue", "DaysPastDue")

         darwillRepository.findActiveCustomers(company).forEach { activeCustomer ->
            activeCustomerCsv.printRecord(
               activeCustomer.storeId,
               activeCustomer.peopleId,
               activeCustomer.uniqueId,
               activeCustomer.firstName ?: EMPTY,
               activeCustomer.lastName ?: EMPTY,
               activeCustomer.address1 ?: EMPTY,
               activeCustomer.address2 ?: EMPTY,
               activeCustomer.city ?: EMPTY,
               activeCustomer.state ?: EMPTY,
               activeCustomer.zip ?: EMPTY,
               activeCustomer.cellPhoneNumber ?: EMPTY,
               activeCustomer.homePhoneNumber ?: EMPTY,
               activeCustomer.email,
               activeCustomer.agreementId,
               activeCustomer.paymentFrequency,
               activeCustomer.textOptIn,
               activeCustomer.onlineIndicator,
               activeCustomer.carePlus,
               activeCustomer.projectedPayout,
               activeCustomer.paymentsLeftInWeeks,
               activeCustomer.pastDue,
               activeCustomer.daysPastDue
            )
         }

         writer.flush() // flush to make sure all the bytes are written to disk before it is closed
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(activeCustomerTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Active customer upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(activeCustomerTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(activeCustomerTempPath)

      return DarwillJobResult("Darwill Active Customers", null, lineCount)
   }
}
