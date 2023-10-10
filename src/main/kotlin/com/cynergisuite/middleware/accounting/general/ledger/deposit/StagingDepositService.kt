package com.cynergisuite.middleware.accounting.general.ledger.deposit

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StagingDepositFilterRequest
import com.cynergisuite.domain.StagingStatusFilterRequest
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure.StagingDepositRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.store.StoreDTO
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import com.cynergisuite.util.CSVUtils.Companion.executeProcess
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.util.UUID

@Singleton
class StagingDepositService @Inject constructor(
   private val stagingDepositRepository: StagingDepositRepository,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val generalLedgerJournalService: GeneralLedgerJournalService,
   @Value("\${cynergi.process.update.isam.summary}")
   private val processUpdateIsamForSummary: Boolean,
) {
   private val logger: Logger = LoggerFactory.getLogger(StagingDepositService::class.java)

   fun fetchAll(company: CompanyEntity, pageRequest: StagingDepositPageRequest): Page<StagingDepositDTO> {
      val found = stagingDepositRepository.findAll(company, pageRequest)

      return found.toPage { entity: StagingDepositEntity ->
         StagingDepositDTO(entity)
      }
   }

   fun fetchAll(company: CompanyEntity, filterRequest: StagingDepositFilterRequest): List<StagingDepositDTO> {
      val found = stagingDepositRepository.findAll(company, filterRequest)

      return found.map { entity: StagingDepositEntity ->
         StagingDepositDTO(entity)
      }
   }

   fun fetchAccountingDetails(company: CompanyEntity, verifyId: UUID): AccountingDetailWrapper {
      val found = stagingDepositRepository.fetchAccountingDetails(company, verifyId)
      return AccountingDetailWrapper(found)
   }

   fun postByDate(company: CompanyEntity, idList: List<UUID>, isAdmin: Boolean){

      val accountEntryList = stagingDepositRepository.findByStagingIds(company, idList, isAdmin)

      //create glJournal for each accountEntry
      if (accountEntryList.isNotEmpty()) {
         accountEntryList.map {
            val account = accountRepository.findOne(it.accountId, company)
            val store = storeRepository.findOne(it.profitCenterNumber, company)
            val source = sourceCodeRepository.findOne(it.sourceId, company)
            val glJournal = GeneralLedgerJournalDTO(
               id = null,
               account = AccountDTO(account!!),
               profitCenter = StoreDTO(store!!),
               date = it.date,
               source = GeneralLedgerSourceCodeDTO(source!!),
               amount = it.credit + it.debit,
               message = it.message
            )
            generalLedgerJournalService.create(glJournal, company)
         }
         stagingDepositRepository.updateMovedPendingJE(company, idList)
         if (processUpdateIsamForSummary) {
            val entityList = stagingDepositRepository.findByListId(company, idList)
            val dtoList = entityList.map { StagingDepositDTO(it) }
            summaryToISAM(dtoList, company)
         }
      } else throw NotFoundException("No Accounting Entries to Post To General Ledger Journal")
   }

   fun postByMonth(company: CompanyEntity, idList: List<UUID>, lastDayOfMonth: LocalDate, isAdmin: Boolean) {
      val accountEntryList = stagingDepositRepository.findByStagingIds(company, idList, isAdmin)

      if (accountEntryList.isEmpty()) {
         throw NotFoundException("No Accounting Entries to Post To General Ledger Journal")
      }

      val glJournals = accountEntryList.groupBy { it.accountId to it.profitCenterNumber }
         .map { (accountStorePair, entries) ->
            val (accountId, profitCenterNumber) = accountStorePair
            val sum = entries.sumOf { it.credit.add(it.debit) }

            val account = accountRepository.findOne(accountId, company)
            val store = storeRepository.findOne(profitCenterNumber, company)
            val source = sourceCodeRepository.findOne(entries[0].sourceId, company)

            GeneralLedgerJournalDTO(
               id = null,
               account = AccountDTO(account!!),
               profitCenter = StoreDTO(store!!),
               date = lastDayOfMonth,
               source = GeneralLedgerSourceCodeDTO(source!!),
               amount = sum,
               message = entries[0].message
            )
         }

      glJournals.forEach {
         generalLedgerJournalService.create(it, company)
         stagingDepositRepository.updateMovedPendingJE(company, idList)
         if (processUpdateIsamForSummary) {
            val entityList = stagingDepositRepository.findByListId(company, idList)
            val dtoList = entityList.map { StagingDepositDTO(it) }
            summaryToISAM(dtoList, company)
         }
      }
   }

   fun summaryToISAM(dtos: List<StagingDepositDTO>, company: CompanyEntity) {
      val fileName = File.createTempFile("mrsummary", ".csv")
      val fileWriter = FileWriter(fileName)
      val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT.withDelimiter('|').withHeader("action", "store", "business_date", "moved_to_pending_journal_entries", "dummy_field"))
      val dataset = company.datasetCode
      try {
         csvPrinter.printRecord("action", "store", "business_date", "moved_to_pending_journal_entries", "dummy_field")
         dtos.forEach {
            if (it.movedToPendingJournalEntries == true) {
               csvPrinter.printRecord(
                  "U",
                  it.store.toString(),
                  it.businessDate.toString(),
                  "Y",
                  "1"
               )
            }
         }
      } catch (e: Exception) {
         logger.error("Error occurred in creating SUMMARY csv file!", e)
      } finally {
         executeProcess(fileName, fileWriter, csvPrinter, logger, dataset, "/usr/bin/ht.updt_isam_summary.sh")
      }
   }

   fun fetchStatus(company: CompanyEntity, filterRequest: StagingStatusFilterRequest): List<StagingDepositStatusDTO> {
      val found = stagingDepositRepository.findAll(company, filterRequest)

      return found
   }
}
