package com.cynergisuite.middleware.reportal

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

   init {
      File(reportalFileLocation).mkdirs()
   }

   fun generateReportalDocument(generator: (reportalOutputStream: OutputStream) -> Unit) {
      logger.debug("Generating reportal document using {}", generator)

      executor.execute {
         val tempFile = File.createTempFile("reportalTemp", "rpt")

         logger.info("Generating reportal document.  Placing in temp file {}", tempFile)

         FileOutputStream(tempFile).use { reportalOutputStream ->
            generator(reportalOutputStream)

            reportalOutputStream.flush()
         }

         val reportalFile = File(File(reportalFileLocation), "${tempFile.name}.pdf")

         logger.debug("Moving file {} to {}", tempFile, reportalFile)
         Files.move(tempFile.toPath(), reportalFile.toPath()) // TODO copy doc to reportal location

         logger.debug("Moving tempFile {} was successful: {}", tempFile, reportalFile.exists())
      }
   }
}
