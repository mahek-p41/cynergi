package com.cynergisuite.domain

import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.Reader

abstract class CSVParsingService {
   protected abstract fun processCsvRow(record: CSVRecord)

   @Transactional
   open fun processCsv(reader: Reader) {
      CSVParser(reader, CSVFormat.EXCEL.withHeader()).use { parser ->
         for (record in parser) {
            processCsvRow(record)
         }
      }
   }
}
