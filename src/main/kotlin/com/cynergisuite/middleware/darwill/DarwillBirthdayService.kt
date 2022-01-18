package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.darwill.infrastructure.DarwillRepository
import com.cynergisuite.middleware.schedule.BeginningOfMonthJob
import com.cynergisuite.middleware.schedule.EndOfMonthJob
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
import java.time.Month
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton

@Singleton
@Named("DarwillBirthday")
class DarwillBirthdayService @Inject constructor(
   areaService: AreaService,
   private val darwillRepository: DarwillRepository,
   private val sftpClientService: SftpClientService,
) : BeginningOfMonthJob, DarwillScheduledService<Month>(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(DarwillBirthdayService::class.java)

   override fun shouldProcess(time: Month): Boolean = true

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: Month): DarwillJobResult {
      val pushedFileName = "${company.clientCode}-birthdays.csv"
      val birthdayTempPath = Files.createTempFile("birthday", ".csv")

      Files.newBufferedWriter(birthdayTempPath).use { writer ->
         val birthdayCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         birthdayCsv.printRecord("StoreId", "PeopleID", "UniqueId", "FirstName", "LastName", "Address1", "Address2", "City", "State", "Zip", "CellPhone", "HomePhone", "Email", "BirthDay")

         darwillRepository.findBirthdays(company, time).forEach { birthday ->
            birthdayCsv.printRecord(
               birthday.storeId,
               birthday.peopleId,
               birthday.uniqueId,
               birthday.firstName,
               birthday.lastName,
               birthday.address1,
               birthday.address2,
               birthday.city,
               birthday.state,
               birthday.zip,
               birthday.cellPhoneNumber,
               birthday.homePhoneNumber,
               birthday.email,
               birthday.birthDay
            )
         }

         writer.flush()

         sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
            FileInputStream(birthdayTempPath.toFile()).channel.use { inputChannel ->
               val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

               logger.debug("Birthday upload bytes transferred {}", transferred)
            }
         }

         val bufferedReader: BufferedReader = Files.newBufferedReader(birthdayTempPath)
         val lineCount: Int = bufferedReader.readLines().count()

         Files.delete(birthdayTempPath)

         return DarwillJobResult("Darwill Birthdays", null, lineCount)
      }
   }
}
