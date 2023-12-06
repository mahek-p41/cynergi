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
@Named("WowFinalPayment") // Must match a row in the schedule_command_type_domain
class WowFinalPaymentJob @Inject constructor(
   areaService: AreaService,
   private val wowRepository: WowRepository,
   private val sftpClientService: SftpClientService,
) : WowScheduledJob(areaService) {
   private val logger: Logger = LoggerFactory.getLogger(WowFinalPaymentJob::class.java)

   @ReadOnly
   override fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult {
      val pushedFileName = "${company.clientCode}-final-payments-$fileDate.csv"
      val finalPaymentTempPath = Files.createTempFile("finalPayment", ".csv")

      Files.newBufferedWriter(finalPaymentTempPath).use { writer ->
         val finalPaymentCsv = CSVPrinter(writer, CSVFormat.EXCEL)

         finalPaymentCsv.printRecord("StoreNumber", "CustomerNumber", "FirstName", "LastName", "Email", "AgreementNumber", "Product", "PayoutDate", "CellNumber","HomeNumber")

         wowRepository.findFinalPayment(company).forEach { finalPayment ->
            finalPaymentCsv.printRecord(
               finalPayment.storeNumber,
               finalPayment.customerNumber,
               finalPayment.firstName ?: EMPTY,
               finalPayment.lastName ?: EMPTY,
               finalPayment.email ?: EMPTY,
               finalPayment.agreementNumber,
               finalPayment.product ?: EMPTY,
               finalPayment.payoutDate,
               finalPayment.cellPhoneNumber ?: EMPTY,
               finalPayment.homePhoneNumber ?: EMPTY
            )
         }

         writer.flush()
      }

      sftpClientService.transferFile(Path.of(pushedFileName), sftpClientCredentials) { fileChannel ->
         FileInputStream(finalPaymentTempPath.toFile()).channel.use { inputChannel ->
            val transferred = inputChannel.transferTo(0, inputChannel.size(), fileChannel)

            logger.debug("Final Payments upload bytes transferred {}", transferred)
         }
      }

      val bufferedReader: BufferedReader = Files.newBufferedReader(finalPaymentTempPath)
      val lineCount: Int = bufferedReader.readLines().count()

      Files.delete(finalPaymentTempPath)

      return WowJobResult("Wow Final Payment", null, lineCount)
   }
}
