package com.cynergisuite.domain

import com.cynergisuite.middleware.legacy.load.LegacyCsvLoadingService
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.Reader

abstract class CSVParsingService : LegacyCsvLoadingService {
   abstract fun processCsvRow(record: CSVRecord)

   @Transactional
   final override fun processCsv(reader: Reader) {
      CSVParser(reader, CSVFormat.EXCEL.withHeader()).use { parser ->
         for (record in parser) {
            processCsvRow(record)
         }
      }
   }
}
