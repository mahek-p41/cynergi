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
@Named("WowSingleAgreement") // Must match a row in the schedule_command_type_domain
class WowSingleAgreementJob @Inject constructor(
   areaService: AreaService,
   private val wowRepository: WowRepository,
   private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowSingleAgreementJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-single-agreements-$fileDate.csv"
      val singleAgreementTempPath = Files.createTempFile("singleAgreement", ".csv")

      Files.newBufferedWriter(singleAgreementTempPath).use { writer ->
         val singleAgreementCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         singleAgreementCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "Product", "Description", "PaymentsRemaining")

         wowRepository.findSingleAgreement(company).forEach { singleAgreement ->
            singleAgreementCsv.printRecord(
               singleAgreement.storeNumber,
               singleAgreement.customerNumber,
               singleAgreement.firstName ?: EMPTY,
               singleAgreement.lastName ?: EMPTY,
               singleAgreement.email ?: EMPTY,
               singleAgreement.agreementNumber,
               singleAgreement.product ?: EMPTY,
               singleAgreement.description ?: EMPTY,
               singleAgreement.paymentsRemaining
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(singleAgreementTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Single Agreement upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(singleAgreementTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(singleAgreementTempPath)

      return WowJobResult("Wow Single Agreements", null, lineCount)
   }
}
