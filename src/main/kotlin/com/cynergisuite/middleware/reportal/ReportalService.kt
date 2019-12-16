package com.cynergisuite.middleware.reportal

import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.threading.CynergiExecutor
import io.micronaut.context.annotation.Value
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Files.createDirectories
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportalService @Inject constructor(
   private val executor: CynergiExecutor,
   @Value("\${cynergi.reportal.file.location}") private val reportalFileLocation: String
) {
   private val logger: Logger = LoggerFactory.getLogger(ReportalService::class.java)
   private val reportalDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy-hhmmss")
   private val reportalDirectory = Paths.get(reportalFileLocation).also { createDirectories(it) }

   fun generateReportalDocument(store: StoreEntity, reportName: String, extension: String, generator: (reportalOutputStream: OutputStream) -> Unit) {
      logger.debug("Generating reportal {} document using {} for store {}", reportName, generator, store)

      val storeDirectory = reportalDirectory.resolve("store${store.number}").also { createDirectories(it) }

      executor.execute {
         val tempPath = Files.createTempFile(reportName, "rpt${store.number}")

         logger.info("Generating reportal document.  Placing in temp file {}", tempPath)

         Files.newOutputStream(tempPath).use { reportalOutputStream ->
            generator(reportalOutputStream)

            reportalOutputStream.flush()
         }

         val reportalFile = storeDirectory.resolve("$reportName-${LocalDateTime.now().format(reportalDateFormat)}.${extension}")

         logger.debug("Moving file {} to {}", tempPath, reportalFile)
         val destPath = Files.move(tempPath, reportalFile)

         logger.debug("Moving tempFile {} to {} was successful: {} and original exists {}", tempPath, destPath, Files.exists(reportalFile), Files.exists(tempPath))
      }
   }
}
