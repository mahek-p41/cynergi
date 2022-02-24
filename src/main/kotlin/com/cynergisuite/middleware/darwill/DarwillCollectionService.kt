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
import org.apache.commons.csv.CSVFormat.EXCEL
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
@Named("DarwillCollection")
class DarwillCollectionService @Inject constructor(
   areaService: AreaService,
   private val darwillRepository: DarwillRepository,
   private val sftpClientService: SftpClientService,
) : OnceDailyJob, DarwillScheduledService<DayOfWeek>(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(DarwillCollectionService::class.java)

   override fun shouldProcess(time: DayOfWeek): Boolean = true

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: DayOfWeek, fileDate: String): DarwillJobResult {
      val pushedFileName = "${company.clientCode}-collections-${fileDate}.csv"
      val collectionTempPath = Files.createTempFile("collection", ".csv")

      Files.newBufferedWriter(collectionTempPath).use { writer ->
         val collectionCsv = CSVPrinter(writer, EXCEL)

         collectionCsv.printRecord("StoreId", "PeopleID", "UniqueId", "FirstName", "LastName", "Address1", "Address2", "City", "State", "Zip", "CellPhone", "HomePhone", "Email", "AgreementId", "DaysLate")

         darwillRepository.findCollections(company).forEach { collection ->
            collectionCsv.printRecord(
               collection.storeId,
               collection.peopleId,
               collection.uniqueId,
               collection.firstName,
               collection.lastName,
               collection.address1,
               collection.address2,
               collection.city ?: EMPTY,
               collection.state ?: EMPTY,
               collection.zip ?: EMPTY,
               collection.cellPhoneNumber ?: EMPTY,
               collection.homePhoneNumber ?: EMPTY,
               collection.email ?: EMPTY,
               collection.agreementId,
               collection.daysLate
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(collectionTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Collection upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(collectionTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(collectionTempPath)

      return DarwillJobResult("Darwill Collections", null, lineCount)
   }
}
