package com.cynergisuite.middleware.inload

import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
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
class InloadMEINVService @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val accountRepository: AccountRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val glJournalRepository: GeneralLedgerJournalRepository,
) : CsvInloaderBase(FileSystems.getDefault().getPathMatcher("glob:MEINV_*")) {

   private val logger: Logger = LoggerFactory.getLogger(InloadMEINVService::class.java)

   override fun inloadCsv(record: CSVRecord, batchId: UUID) {
      logger.debug("Loading month end general ledger journal record MEINV {}", record)

      // Todo: make some improvements on next task
      val company = companyRepository.findByDataset(record["Data_Set_ID"].trim())!!
      val account = accountRepository.findByNumber(record["Account_Number"].toLong(), company)!!
      val source = sourceCodeRepository.findOne(record["Source_Code"], company)!!

      val map = mapOf(
         "company_id" to company.id,
         "account_id" to account.id,
         "profit_center_id_sfk" to record["Profit_Center_Number"].toInt(),
         "date" to LocalDate.parse(record["JE_Date"]),
         "source_id" to source.id,
         "amount" to record["JE_Amount"].toBigDecimal(),
         "message" to null
      )
      glJournalRepository.insert(record, map)

   }

}
