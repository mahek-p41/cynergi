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
import kotlin.io.path.name

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
         "DEP_1" to record["Dep_Cash_Amt"].toBigDecimal(),
         "DEP_2" to record["Dep_For_Oth_Str_Amt"].toBigDecimal(),
         "DEP_3" to record["Dep_From_Oth_Str_Amt"].toBigDecimal(),
         "DEP_4" to record["Dep_CC_In_Str_Amt"].toBigDecimal(),
         "DEP_5" to record["Dep_ACH_OLP_Amt"].toBigDecimal(),
         "DEP_6" to record["Dep_CC_OLP_Amt"].toBigDecimal(),
         "DEP_7" to record["Dep_Debit_Card_Amt"].toBigDecimal(),
         "message" to null
      )
      generalLedgerInterfaceRepository.upsert(record, map)

   }

   @Transactional
   override fun inload(reader: BufferedReader, path: Path?): Int {
      var rowsLoaded = 0
      val batchId = UUID.randomUUID()
      val company = companyRepository.findByDataset(extractDataset(path!!.name)!!)!!
      val dateStorePairs: MutableSet<Pair<LocalDate, Int>> = HashSet()

      try {
         CSVParser(reader, CSVFormat.EXCEL.withHeader().withDelimiter('|')).use { parser ->
            for (record in parser) {
               dateStorePairs.add(Pair(LocalDate.parse(record["Date"]), record["Store_Number"].toInt()))
               inloadCsv(record, batchId)
               rowsLoaded++
            }
         }

         val groupedStoresByDate: Map<LocalDate, List<Int>> = dateStorePairs.groupBy({ it.first }) { it.second }
         groupedStoresByDate.forEach { (date, uploadedStores) ->
            generalLedgerInterfaceRepository.deleteStagingDataFromObsoleteStores(company.id!!, date, uploadedStores.toList())
         }
      } catch (t: Throwable) {
         logger.error("Error occurred importing CSV", t)
         throw t
      }

      return rowsLoaded
   }

}
