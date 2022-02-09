package com.cynergisuite.middleware.reportal

import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.threading.CynergiExecutor
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.SystemUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Files.createDirectories
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE
import java.nio.file.attribute.PosixFilePermission.GROUP_READ
import java.nio.file.attribute.PosixFilePermission.GROUP_WRITE
import java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OTHERS_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Singleton
class ReportalService @Inject constructor(
   private val executor: CynergiExecutor,
   @Value("\${cynergi.reportal.file.location}") private val reportalFileLocation: String
) {
   private val logger: Logger = LoggerFactory.getLogger(ReportalService::class.java)
   private val reportalDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy-hhmmss")
   private val reportalDirectory = Paths.get(reportalFileLocation).also { createDirectories(it) }

   fun generateReportalDocument(store: Store, reportName: String, extension: String, generator: (reportalOutputStream: OutputStream) -> Unit) {
      logger.debug("Generating reportal {} document using {} for store {}", reportName, generator, store)

      val storeDirectory = reportalDirectory.resolve("store${store.myNumber()}").also { createDirectories(it) }

      executor.execute {
         val tempPath = Files.createTempFile(reportName, "rpt${store.myNumber()}")

         logger.info("Generating reportal document.  Placing in temp file {}", tempPath)

         Files.newOutputStream(tempPath).use { reportalOutputStream ->
            generator(reportalOutputStream)

            reportalOutputStream.flush()
         }

         val reportalFile = storeDirectory.resolve("$reportName-${LocalDateTime.now().format(reportalDateFormat)}.$extension")

         logger.debug("Moving file {} to {}", tempPath, reportalFile)
         val destPath = Files.move(tempPath, reportalFile)

         if (SystemUtils.IS_OS_LINUX) {
            Files.setPosixFilePermissions(destPath, setOf(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_WRITE, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE))
         }

         logger.debug("Moving tempFile {} to {} was successful: {} and original exists {}", tempPath, destPath, Files.exists(reportalFile), Files.exists(tempPath))
      }
   }
}
