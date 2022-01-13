package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.darwill.infrastructure.DarwillRepository
import com.cynergisuite.middleware.schedule.OnceDailyJob
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import com.cynergisuite.middleware.ssh.SftpClientService
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton

@Singleton
@Named("DarwillLastWeeksDelivery")
class DarwillLastWeeksDeliveryService @Inject constructor(
   areaService: AreaService,
   private val darwillRepository: DarwillRepository,
   private val sftpClientService: SftpClientService,
) : OnceDailyJob, DarwillScheduledService<DayOfWeek>(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(DarwillLastWeeksDeliveryService::class.java)

   override fun shouldProcess(time: DayOfWeek): Boolean = time == MONDAY

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: DayOfWeek): DarwillJobResult {
      val pushedFileName = "${company.clientCode}-last-week-deliveries.csv"
      val lastWeeksDeliveryTempPath = Files.createTempFile("lastWeeksDelivery", ".csv")

      Files.newBufferedWriter(lastWeeksDeliveryTempPath).use { writer ->
         val lastWeeksDeliveryCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         lastWeeksDeliveryCsv.printRecord("StoreId", "PeopleID", "UniqueId", "FirstName", "LastName", "Address1", "Address2", "City", "State", "Zip", "CellPhone", "HomePhone", "Email", "AgreementId", "PurchaseDate", "CurrentCustomerStatus", "NewCustomer")

         darwillRepository.findLastWeeksDeliveries(company).forEach { lastWeeksDelivery ->
            lastWeeksDeliveryCsv.printRecord(
               lastWeeksDelivery.storeId,
               lastWeeksDelivery.peopleId,
               lastWeeksDelivery.uniqueId,
               lastWeeksDelivery.firstName,
               lastWeeksDelivery.lastName,
               lastWeeksDelivery.address1,
               lastWeeksDelivery.address2,
               lastWeeksDelivery.city,
               lastWeeksDelivery.state,
               lastWeeksDelivery.zip,
               lastWeeksDelivery.cellPhoneNumber,
               lastWeeksDelivery.homePhoneNumber,
               lastWeeksDelivery.email,
               lastWeeksDelivery.agreementId,
               lastWeeksDelivery.purchaseDate,
               lastWeeksDelivery.currentCustomerStatus,
               lastWeeksDelivery.newCustomer
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(lastWeeksDeliveryTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Last weeks deliveries upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(lastWeeksDeliveryTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(lastWeeksDeliveryTempPath)

      return DarwillJobResult("Darwill Last Weeks Deliveries", null, lineCount)
   }
}
