package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import com.cynergisuite.middleware.ssh.SftpClientService
import com.cynergisuite.middleware.wow.infrastructure.WowRepository
import com.cynergisuite.middleware.wow.schedule.spi.WowScheduledJob
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
import java.time.OffsetDateTime

@Singleton
@Named("WowCollection") // Must match a row in the schedule_command_type_domain
class WowCollectionJob @Inject constructor(
   areaService: AreaService,
   private val wowRepository: WowRepository,
   private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowCollectionJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-wow-collections-$fileDate.csv"
      val wowcollectionTempPath = Files.createTempFile("wowcollection", ".csv")

      Files.newBufferedWriter(wowcollectionTempPath).use { writer ->
         val wowcollectionCsv = CSVPrinter(writer, EXCEL)

         wowcollectionCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "DaysOverdue", "OverdueAmount", "Product", "CellNumber","HomeNumber")

         wowRepository.findWowCollections(company).forEach { wowcollection ->
            wowcollectionCsv.printRecord(
               wowcollection.storeNumber,
               wowcollection.customerNumber,
               wowcollection.firstName ?: EMPTY,
               wowcollection.lastName ?: EMPTY,
               wowcollection.email ?: EMPTY,
               wowcollection.agreementNumber,
               wowcollection.daysOverdue,
               wowcollection.overdueAmount,
               wowcollection.product,
               wowcollection.cellPhoneNumber,
               wowcollection.homePhoneNumber
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(wowcollectionTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Wow Collection upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(wowcollectionTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(wowcollectionTempPath)

      return WowJobResult("Wow Collections", null, lineCount)
   }
}
