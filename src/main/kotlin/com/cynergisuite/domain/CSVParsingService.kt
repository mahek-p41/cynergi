package com.cynergisuite.domain

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.Reader

abstract class CSVParsingService {

   abstract fun processCsv(reader: Reader)

   protected fun processCsv(reader: Reader, rowProcessor: (record: CSVRecord) -> Unit) {
      CSVParser(reader, CSVFormat.EXCEL.withHeader()).use { parser ->
         parser.forEach(rowProcessor)
      }
   }
}
