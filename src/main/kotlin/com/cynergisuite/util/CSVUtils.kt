package com.cynergisuite.util

import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.io.FileWriter
import java.util.concurrent.TimeUnit

class CSVUtils {
   companion object {
      fun executeProcess(fileName: File, fileWriter: FileWriter, csvPrinter: CSVPrinter, logger: Logger, dataset: String, scriptPath: String) {
         try {
            fileWriter.flush()
            fileWriter.close()
            csvPrinter.close()
            val processExecutor: ProcessExecutor = ProcessExecutor()
               .command("/bin/bash", scriptPath, fileName.canonicalPath, dataset)
               .exitValueNormal()
               .timeout(5, TimeUnit.SECONDS)
               .readOutput(true)
            logger.debug(processExecutor.execute().outputString())
         } catch (e: Throwable) {
            logger.error("Error occurred in creating the CSV file!", e)
         }
      }
   }
}
