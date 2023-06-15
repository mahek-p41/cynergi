package com.cynergisuite.middleware.inload

import com.cynergisuite.extensions.fileExists
import com.cynergisuite.extensions.tryLockForReader
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService

@Singleton
class InloadService @Inject constructor(
   @Named("inload")
   private val executorService: ExecutorService,
   private val inloadMEINVService: InloadMEINVService,
   private val inloadSUMGLDETService: InloadSUMGLDETService
) {
   private val logger: Logger = LoggerFactory.getLogger(InloadService::class.java)

   fun processPath(path: Path) {
      logger.info("Processing inload file {}", path)

      executorService.submit {
         try {
            if (path.fileExists()) {
               logger.info("{} existed", path)

               val baseFileName = extractBaseFileName(path)

               logger.debug("Searching for Inloader using {}", baseFileName)

               if (inloadMEINVService.canProcess(path)) {
                  logger.info("Processing {} with {}", path, inloadMEINVService)

                  val lockedSuccessfully = path.tryLockForReader { reader ->
                     inloadMEINVService.inload(reader)
                  }

                  if (lockedSuccessfully) {
                     val target = path.resolveSibling("archive/${path.fileName}.PROCESSED")

                     if (!Files.exists(target.parent)) {
                        Files.createDirectories(target.parent)
                     }

                     logger.debug("Moving {} to {}", path, target)

                     Files.move(path, target)
                  } else {
                     logger.warn("Unable to lock {}", path)
                  }
               }

               if (inloadSUMGLDETService.canProcess(path)) {
                  logger.info("Processing {} with {}", path, inloadSUMGLDETService)

                  val lockedSuccessfully = path.tryLockForReader { reader ->
                     inloadSUMGLDETService.inload(reader)
                  }

                  if (lockedSuccessfully) {
                     val target = path.resolveSibling("archive/${path.fileName}.PROCESSED")

                     if (!Files.exists(target.parent)) {
                        Files.createDirectories(target.parent)
                     }

                     logger.debug("Moving {} to {}", path, target)

                     Files.move(path, target)
                  } else {
                     logger.warn("Unable to lock {}", path)
                  }
               }
            } else {
               logger.warn("{} did not exist", path)
            }

         } catch (e: Throwable) {
            logger.error("Error occurred during inloading of {}", path)
         }

      }
   }

   private fun extractBaseFileName(path: Path): String {
      var underScoreCount = 0

      return path.fileName.toString().fold("") { acc, c ->
         if (c == '_') {
            underScoreCount++
         }

         if (underScoreCount < 2) {
            "${acc}$c"
         } else {
            acc
         }
      }
   }

}
