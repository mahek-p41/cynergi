package com.cynergisuite.middleware.inload

import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure.GeneralLedgerInterfaceRepository
import com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure.StagingDepositTypeRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.NotFoundException
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
class InloadSUMGLINTAService @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val accountRepository: AccountRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val generalLedgerInterfaceRepository: GeneralLedgerInterfaceRepository,
   private val stagingDepositTypeRepository: StagingDepositTypeRepository,
) : CsvInloaderBase(FileSystems.getDefault().getPathMatcher("glob:SUMGLINTA_*.COMPLETE")) {

   private val logger: Logger = LoggerFactory.getLogger(InloadSUMGLINTAService::class.java)

   override fun inloadCsv(record: CSVRecord, batchId: UUID) {
      logger.debug("Loading daily general ledger journal record SUMGLINTA {}", record)

      val company = companyRepository.findByDataset(record["Data_Set_ID"].trim())!!
      val account = accountRepository.findByNumber(record["Account_Number"].toLong(), company)!!
      val source = sourceCodeRepository.findOne(record["Source_Code"], company)!!
      val verifyId = generalLedgerInterfaceRepository.fetchVerifyId(company.id, record["Store"].toInt(), LocalDate.parse(record["JE_Date"])) ?: throw NotFoundException("Unable to find corresponding verify ID")
      val depositType = stagingDepositTypeRepository.findOne(value = record["Deposit_Type"])
      val movedToPendingJEs = generalLedgerInterfaceRepository.movedToPendingJournalEntries(company.id!!, record["Store"].toInt(), LocalDate.parse(record["JE_Date"]))

      if (!movedToPendingJEs) {
         val map = mapOf(
            "company_id" to company.id,
            "verify_id" to verifyId,
            "store_number_sfk" to record["Store"].toInt(),
            "business_date" to LocalDate.parse(record["JE_Date"]),
            "account_id" to account.id,
            "profit_center_id_sfk" to record["Profit_Center_Number"].toInt(),
            "source_id" to source.id,
            "journal_entry_amount" to record["JE_Amount"].toBigDecimal(),
            "deposit_type_id" to depositType?.id,
            "message" to record["Message"],
         )
         generalLedgerInterfaceRepository.insertStagingAccountEntries(record, map)
      }
   }

   @Transactional
   override fun inload(reader: BufferedReader, path: Path?): Int {
      var rowsLoaded = 0
      val batchId = UUID.randomUUID()

      val company = companyRepository.findByDataset(extractDataset(path!!.name)!!)!!

      val dateStorePairs: MutableSet<Pair<LocalDate, Int>> = HashSet()
      val csvRecords = mutableListOf<CSVRecord>()
      try {
         CSVParser(reader, CSVFormat.EXCEL.withHeader().withDelimiter('|')).use { parser ->
            for (record in parser) {
               dateStorePairs.add(Pair(LocalDate.parse(record["JE_Date"]), record["Store"].toInt()))
               csvRecords.add(record)
               rowsLoaded++
            }
         }
         // clean data from accounting_entries_staging for uploaded date-store pairs
         val groupedStoresByDate: Map<LocalDate, List<Int>> = dateStorePairs.groupBy({ it.first }) { it.second }
         groupedStoresByDate.forEach { (date, uploadedStores) ->
            generalLedgerInterfaceRepository.deleteUnmovedStagingAccountEntries(company.id!!, date, uploadedStores.toList())
         }

         // insert new data for accounting_entries_staging
         csvRecords.forEach {
            inloadCsv(it, batchId)
         }

      } catch (t: Throwable) {
         logger.error("Error occurred importing CSV", t)
         throw t
      }

      return rowsLoaded
   }
}
