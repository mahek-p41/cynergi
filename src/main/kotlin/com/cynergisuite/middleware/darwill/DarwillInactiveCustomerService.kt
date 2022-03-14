package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.darwill.infrastructure.DarwillRepository
import com.cynergisuite.middleware.darwill.spi.DarwillScheduledService
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
import java.time.DayOfWeek.SUNDAY

@Singleton
@Named("DarwillInactiveCustomer")
class DarwillInactiveCustomerService @Inject constructor(
   areaService: AreaService,
   private val darwillRepository: DarwillRepository,
   private val sftpClientService: SftpClientService,
) : OnceDailyJob, DarwillScheduledService<DayOfWeek>(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(DarwillInactiveCustomerService::class.java)

   override fun shouldProcess(time: DayOfWeek): Boolean = time == SUNDAY

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: DayOfWeek, fileDate: String): DarwillJobResult {
      val pushedFileName = "${company.clientCode}-inactive-customers-${fileDate}.csv"
      val inactiveCustomerTempPath = Files.createTempFile("inactiveCustomer", ".csv")

      Files.newBufferedWriter(inactiveCustomerTempPath).use { writer ->
         val inactiveCustomerCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         inactiveCustomerCsv.printRecord("StoreId", "PeopleId", "UniqueId", "FirstName", "LastName", "Address1", "Address2", "City", "State", "Zip", "CellPhone", "HomePhone", "Email", "BirthDay", "AgreementId", "InactiveDate", "ReasonIndicator", "Reason", "AmountPaid", "CustomerRating")

         darwillRepository.findInactiveCustomers(company).forEach { inactiveCustomer ->
            inactiveCustomerCsv.printRecord(
               inactiveCustomer.storeId,
               inactiveCustomer.peopleId,
               inactiveCustomer.uniqueId,
               inactiveCustomer.firstName ?: EMPTY,
               inactiveCustomer.lastName ?: EMPTY,
               inactiveCustomer.address1 ?: EMPTY,
               inactiveCustomer.address2 ?: EMPTY,
               inactiveCustomer.city ?: EMPTY,
               inactiveCustomer.state ?: EMPTY,
               inactiveCustomer.zip ?: EMPTY,
               inactiveCustomer.cellPhoneNumber ?: EMPTY,
               inactiveCustomer.homePhoneNumber ?: EMPTY,
               inactiveCustomer.email ?: EMPTY,
               inactiveCustomer.birthDay,
               inactiveCustomer.agreementId,
               inactiveCustomer.inactiveDate,
               inactiveCustomer.reasonIndicator,
               inactiveCustomer.reason,
               inactiveCustomer.amountPaid,
               inactiveCustomer.customerRating
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(inactiveCustomerTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Inactive customer upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(inactiveCustomerTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(inactiveCustomerTempPath)

      return DarwillJobResult("Darwill Inactive Customers", null, lineCount)
   }
}
