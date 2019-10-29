package com.cynergisuite.middleware.reportal

import io.micronaut.context.annotation.Value
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportalService @Inject constructor(
   private val executorService: ExecutorService,
   @Value("\${cynergi.reportal.file.location}") private val reportalFileLocation: String
) {
   private val logger: Logger = LoggerFactory.getLogger(ReportalService::class.java)

   fun generateReportalDocument(generator: (reportalOutputStream: OutputStream) -> Unit) {
      logger.debug("Generating reportal document using {}", generator)

      executorService.execute {
         val tempFile = File.createTempFile("reportalTemp", "rpt")

         logger.info("Generating reportal document.  Placing in temp file {}", tempFile)

         FileOutputStream(tempFile).use { reportalOutputStream ->
            generator(reportalOutputStream)

            reportalOutputStream.flush()
         }

         // TODO copy doc to reportal location
      }
   }
}
