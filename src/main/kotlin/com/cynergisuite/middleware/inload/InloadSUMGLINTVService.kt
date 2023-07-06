package com.cynergisuite.middleware.inload

import com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure.GeneralLedgerInterfaceRepository
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.time.LocalDate
import java.util.UUID

@Singleton
class InloadSUMGLINTVService @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val generalLedgerInterfaceRepository: GeneralLedgerInterfaceRepository,
) : CsvInloaderBase(FileSystems.getDefault().getPathMatcher("glob:SUMGLINTV_*")) {

   private val logger: Logger = LoggerFactory.getLogger(InloadSUMGLINTVService::class.java)

   override fun inloadCsv(record: CSVRecord, batchId: UUID) {
      logger.debug("Loading daily current state record SUMGLINTV {}", record)

      // Todo: make some improvements on next task
      val company = companyRepository.findByDataset(record["Data_Set_ID"].trim())!!

      val map: MutableMap<String, Any?> = mutableMapOf(
         "company_id" to company.id,
         "store_number_sfk" to record["Store_Number"].toInt(),
         "business_date" to LocalDate.parse(record["Date"]),
         "verify_successful" to record["Verify_Successful"].toBoolean(),
         "error_amount" to record["Error_Amount"].toBigDecimal(),
         "moved_to_pending_journal_entries" to false,
         "DEP_1" to record["Dep_Cash_Amt"].toBigDecimal(),
         "DEP_2" to record["Dep_For_Oth_Str_Amt"].toBigDecimal(),
         "DEP_3" to record["Dep_From_Oth_Str_Amt"].toBigDecimal(),
         "DEP_4" to record["Dep_CC_In_Str_Amt"].toBigDecimal(),
         "DEP_5" to record["Dep_ACH_OLP_Amt"].toBigDecimal(),
         "DEP_6" to record["Dep_CC_OLP_Amt"].toBigDecimal(),
         "DEP_7" to record["Dep_Debit_Card_Amt"].toBigDecimal(),
         "message" to null
      )
      generalLedgerInterfaceRepository.insert(record, map)

   }

}
