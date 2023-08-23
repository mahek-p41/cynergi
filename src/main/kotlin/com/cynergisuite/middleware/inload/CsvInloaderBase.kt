package com.cynergisuite.middleware.inload

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.UUID
import javax.transaction.Transactional
import kotlin.io.path.Path

abstract class CsvInloaderBase(
   private val pathMatcher: PathMatcher
) : Inloader {
   private val logger: Logger = LoggerFactory.getLogger(CsvInloaderBase::class.java)

   protected abstract fun inloadCsv(record: CSVRecord, batchId: UUID)

   override fun canProcess(path: Path): Boolean {
      val filename = path.fileName.toString().uppercase()
      val matched = pathMatcher.matches(Path(filename))

      logger.debug("Checking if path {} matches {}", filename, matched)

      return matched
   }

   @Transactional
   override fun inload(reader: BufferedReader, path: Path?): Int {
      var rowsLoaded = 0
      val batchId = UUID.randomUUID()

      try {
         CSVParser(reader, CSVFormat.EXCEL.withHeader().withDelimiter('|')).use { parser ->
            for (record in parser) {
               inloadCsv(record, batchId)
               rowsLoaded++
            }
         }
      } catch (t: Throwable) {
         logger.error("Error occurred importing CSV", t)
         throw t
      }

      return rowsLoaded
   }

   fun extractDataset(input: String): String? {
      val pattern = "_(\\w+)_\\d{8}" // Matches a word between two underscores

      val matcher = Regex(pattern).find(input)
      if (matcher != null) {
         return matcher.groups[1]?.value
      }

      return null
   }
}
