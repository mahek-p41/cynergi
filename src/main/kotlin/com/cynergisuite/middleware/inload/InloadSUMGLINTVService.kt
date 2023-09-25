package com.cynergisuite.middleware.inload

import com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure.GeneralLedgerInterfaceRepository
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.nio.file.FileSystems
import java.nio.file.Path
import java.time.LocalDate
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class InloadSUMGLINTVService @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val generalLedgerInterfaceRepository: GeneralLedgerInterfaceRepository,
) : CsvInloaderBase(FileSystems.getDefault().getPathMatcher("glob:SUMGLINTV_*.COMPLETE")) {

   private val logger: Logger = LoggerFactory.getLogger(InloadSUMGLINTVService::class.java)

   override fun inloadCsv(record: CSVRecord, batchId: UUID) {
      logger.debug("Loading daily current state record SUMGLINTV {}", record)

      val company = companyRepository.findByDataset(record["Data_Set_ID"].trim())!!

      val map: MutableMap<String, Any?> = mutableMapOf(
         "company_id" to company.id,
         "store_number_sfk" to record["Store_Number"].toInt(),
         "business_date" to LocalDate.parse(record["Date"]),
         "verify_successful" to record["Verify_Successful"].toBoolean(),
         "error_amount" to record["Error_Amount"].toBigDecimal(),
         "moved_to_pending_journal_entries" to false,
         "DEP_1" to Pair(record["Dep_Cash_Amt"].toBigDecimal(), record["Dep_Cash_GL_Acct_Nbr"].toLong()),
         "DEP_2" to Pair(record["Dep_For_Oth_Str_Amt"].toBigDecimal(), record["Dep_For_Oth_Str_GL_Acct_Nbr"].toLong()),
         "DEP_3" to Pair(record["Dep_From_Oth_Str_Amt"].toBigDecimal(), record["Dep_From_Oth_Str_GL_Acct_Nbr"].toLong()),
         "DEP_4" to Pair(record["Dep_CC_In_Str_Amt"].toBigDecimal(), record["Dep_CC_In_Str_GL_Acct_Nbr"].toLong()),
         "DEP_5" to Pair(record["Dep_ACH_OLP_Amt"].toBigDecimal(), record["Dep_ACH_OLP_GL_Acct_Nbr"].toLong()),
         "DEP_6" to Pair(record["Dep_CC_OLP_Amt"].toBigDecimal(), record["Dep_CC_OLP_GL_Acct_Nbr"].toLong()),
         "DEP_7" to Pair(record["Dep_Debit_Card_Amt"].toBigDecimal(), record["Dep_Debit_Card_GL_Acct_Nbr"].toLong()),
         "DEP_8" to Pair(record["ACH_Chargeback_Amt"].toBigDecimal(), record["ACH_Chargeback_GL_Acct_Nbr"].toLong()),
         "DEP_9" to Pair(record["ICC_Chargeback_Amt"].toBigDecimal(), record["ICC_Chargeback_GL_Acct_Nbr"].toLong()),
         "DEP_10" to Pair(record["NSF_Return_Check_Amt"].toBigDecimal(), record["NSF_Return_Check_GL_Acct_Nbr"].toLong()),
         "DEP_11" to Pair(record["AR_Bad_Check_Amt"].toBigDecimal(), record["AR_Bad_Check_GL_Acct_Nbr"].toLong()),
         "message" to null
      )
      generalLedgerInterfaceRepository.upsert(record, map, company)

   }

   @Transactional
   override fun inload(reader: BufferedReader, path: Path?): Int {
      var rowsLoaded = 0
      val batchId = UUID.randomUUID()

      try {
         CSVParser(reader, CSVFormat.EXCEL.withHeader().withDelimiter('|')).use { parser ->
            // upsert data for each date-store pair don't delete data from stores not appearing in csv file
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

}
