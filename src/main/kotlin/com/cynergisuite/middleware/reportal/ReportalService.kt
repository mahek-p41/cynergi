package com.cynergisuite.middleware.reportal

import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.threading.CynergiExecutor
import io.micronaut.context.annotation.Value
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportalService @Inject constructor(
   private val executor: CynergiExecutor,
   @Value("\${cynergi.reportal.file.location}") private val reportalFileLocation: String
) {
   private val logger: Logger = LoggerFactory.getLogger(ReportalService::class.java)

   private val reportalDirectory = File(reportalFileLocation)
   init {
      reportalDirectory.mkdirs()
   }

   fun generateReportalDocument(store: StoreEntity, reportName: String, extension: String, generator: (reportalOutputStream: OutputStream) -> Unit) {
      logger.debug("Generating reportal document using {}", generator)

      val storeDirectory = File(reportalDirectory, "store${store.number}")
      storeDirectory.mkdirs()

      executor.execute {
         //val tempFile = File.createTempFile("reportalTemp", "rpt${store.number}")
         val tempFile = File.createTempFile("${reportName}", "rpt${store.number}")

         logger.info("Generating reportal document.  Placing in temp file {}", tempFile)

         FileOutputStream(tempFile).use { reportalOutputStream ->
            generator(reportalOutputStream)

            reportalOutputStream.flush()
         }

         val reportalFile = File(storeDirectory, "${tempFile.name}.${extension}")

         logger.debug("Moving file {} to {}", tempFile, reportalFile)
         Files.move(tempFile.toPath(), reportalFile.toPath()) // TODO copy doc to reportal location

         logger.debug("Moving tempFile {} was successful: {}", tempFile, reportalFile.exists())
      }
   }
}
