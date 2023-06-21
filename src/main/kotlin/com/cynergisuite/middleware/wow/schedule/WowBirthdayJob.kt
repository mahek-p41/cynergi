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
@Named("WowBirthday") // Must match a row in the schedule_command_type_domain
class WowBirthdayJob @Inject constructor(
   areaService: AreaService,
   private val wowRepository: WowRepository,
   private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowBirthdayJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-wow-birthdays-$fileDate.csv"
      val wowbirthdayTempPath = Files.createTempFile("wowbirthday", ".csv")

      Files.newBufferedWriter(wowbirthdayTempPath).use { writer ->
         val wowbirthdayCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         wowbirthdayCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "BirthDay")

         wowRepository.findWowBirthday(company).forEach { wowbirthday ->
            wowbirthdayCsv.printRecord(
               wowbirthday.storeNumber,
               wowbirthday.customerNumber,
               wowbirthday.firstName ?: EMPTY,
               wowbirthday.lastName ?: EMPTY,
               wowbirthday.email ?: EMPTY,
               wowbirthday.birthDay
            )
         }

         writer.flush()

         sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
            FileInputStream(wowbirthdayTempPath.toFile()).channel.use { inputChannel ->
               val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

               logger.debug("Wow Birthday upload bytes transferred {}", transferred)
            }
         }

         val bufferedReader: BufferedReader = Files.newBufferedReader(wowbirthdayTempPath)
         val lineCount: Int = bufferedReader.readLines().count()

         Files.delete(wowbirthdayTempPath)

         return WowJobResult("Wow Birthdays", null, lineCount)
      }
   }
}
