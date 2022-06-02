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
@Named("WowActiveInventory") // Must match a row in the schedule_command_type_domain
class WowActiveInventoryJob @Inject constructor(
        areaService: AreaService,
        private val wowRepository: WowRepository,
        private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowActiveInventoryJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-active-inventory-$fileDate.csv"
      val activeInventoryTempPath = Files.createTempFile("activeInventory", ".csv")

      Files.newBufferedWriter(activeInventoryTempPath).use { writer -> // write a csv to the temp file defined above
         val activeInventoryCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         activeInventoryCsv.printRecord("StoreNumber", "sku", "ItemName", "ItemDescription", "TotalQuantity")

         wowRepository.findActiveInventory(company).forEach { activeInventory ->
            activeInventoryCsv.printRecord(
               activeInventory.storeNumber,
               activeInventory.sku,
               activeInventory.itemName ?: EMPTY,
               activeInventory.itemDescription ?: EMPTY,
               activeInventory.totalQuantity ?: EMPTY
            )
         }

         writer.flush() // flush to make sure all the bytes are written to disk before it is closed
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(activeInventoryTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Active inventory upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(activeInventoryTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(activeInventoryTempPath)

      return WowJobResult("Wow Active Inventory", null, lineCount)
   }
}
