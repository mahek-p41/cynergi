package com.cynergisuite.middleware

import com.cynergisuite.middleware.legacy.load.LegacyLoad
import com.cynergisuite.middleware.legacy.load.infrastructure.LegacyLoadRepository
import io.micronaut.configuration.dbmigration.flyway.event.MigrationFinishedEvent
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.codec.binary.Hex
import org.apache.commons.io.input.TeeInputStream
import org.apache.commons.io.output.NullOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.security.DigestOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyDataLoader @Inject constructor(
   @Value("\${cynergi.legacy.import.location}") private val legacyImportLocation: String,
   @Value("\${cynergi.legacy.import.rename}") private val rename: Boolean = true,
   @Value("\${cynergi.legacy.import.rename-extension}") private val renameExtension: String = "processed",
   @Value("\${cynergi.legacy.import.process-startup}") private val processImportsOnStartup: Boolean = true,
   private val legacyImportationRepository: LegacyLoadRepository
) : ApplicationEventListener<MigrationFinishedEvent> {
   private val logger: Logger = LoggerFactory.getLogger(LegacyDataLoader::class.java)
   private val fileSystem = FileSystems.getDefault()
   private val eliMatcher = fileSystem.getPathMatcher("glob:eli*csv")

   override fun onApplicationEvent(event: MigrationFinishedEvent?) {
      if (processImportsOnStartup) {
         processLegacyImports(Paths.get(legacyImportLocation))
      }
   }

   @Transactional
   fun processLegacyImports(importLocation: Path) {
      logger.info("Loading Legacy Data")

      Files.newDirectoryStream(importLocation).use { directoryStream ->
         directoryStream.asSequence()
            .filter { p -> p.toFile().isFile } // filter out anything that isn't a file
            .filter { p -> eliMatcher.matches(p.fileName) } // filter out anything that doesn't end in .csv
            .filter { p -> !legacyImportationRepository.exists(p.toRealPath(LinkOption.NOFOLLOW_LINKS)) } // filter out anything that has already been saved with that name in the database
            .map { path -> processFile(path) } // read in file and save to appropriate table in the database
            .onEach { processed -> saveInLegacyImport(processed) } // safe file and meta in database
            .forEach { processed -> moveProcessedFile(processed) } // move the file to processed
      }

      logger.info("Finished loading Legacy Data")
   }

   private fun processFile(path: Path): Pair<Path, String> {
      val hash: MessageDigest = MessageDigest.getInstance("SHA-256")

      Files.newInputStream(path).use { inputStream ->
         val digestOutputStream = DigestOutputStream(NullOutputStream(), hash)
         val teeInputStream = TeeInputStream(inputStream, digestOutputStream)

         InputStreamReader(teeInputStream).use { reader ->
            BufferedReader(reader).use { bufferedReader ->
               // TODO add in logic for reading in data
            }
         }

         digestOutputStream.flush()
      }

      val hashValue = Hex.encodeHexString(hash.digest())

      logger.info("Imported {} with hash of {}", path, hashValue)

      return Pair(path, hashValue)
   }

   private fun saveInLegacyImport(processed: Pair<Path, String>) {
      legacyImportationRepository.insert(
         LegacyLoad(
            filename = processed.first,
            hash = processed.second
         )
      )
   }

   private fun moveProcessedFile(processed: Pair<Path, String>) {
      val path = processed.first

      if (rename) {
         Files.move(path, path.resolveSibling("${path.fileName}.$renameExtension"))
      }
   }
}
