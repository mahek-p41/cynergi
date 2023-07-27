package com.cynergisuite.middleware.accounting.general.ledger.deposit

import com.cynergisuite.domain.Page
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
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Singleton
class StagingDepositService @Inject constructor(
   private val stagingDepositRepository: StagingDepositRepository,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val generalLedgerJournalService: GeneralLedgerJournalService
) {
   fun fetchAll(company: CompanyEntity, pageRequest: StagingDepositPageRequest): Page<StagingDepositDTO> {
      val found = stagingDepositRepository.findAll(company, pageRequest)

      return found.toPage { entity: StagingDepositEntity ->
         StagingDepositDTO(entity)
      }
   }

   fun fetchAccountingDetails(company: CompanyEntity, verifyId: UUID): AccountingDetailWrapper {
      val found = stagingDepositRepository.fetchAccountingDetails(company, verifyId)
      return AccountingDetailWrapper(found)
   }

   fun postByDate(company: CompanyEntity, dto: List<StagingDepositDTO>){
      val stagingIds = dto.map { it.id }
      val accountEntryList = stagingDepositRepository.findByStagingIds(company, stagingIds)
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
         stagingDepositRepository.updateMovedPendingJE(company, stagingIds)
      } else throw NotFoundException("No Accounting Entries to Post To General Ledger Journal")
   }

   fun postByMonth(company: CompanyEntity, dto: List<StagingDepositDTO>, lastDayOfMonth: LocalDate){
      val stagingIds = dto.map { it.id }
      val accountEntryList = stagingDepositRepository.findByStagingIds(company, stagingIds)
      //combine all accountEntries into one glJournal
      if (accountEntryList.isNotEmpty()) {
         val account = accountRepository.findOne(accountEntryList[0].accountId, company)
         val store = storeRepository.findOne(accountEntryList[0].profitCenterNumber, company)
         val source = sourceCodeRepository.findOne(accountEntryList[0].sourceId, company)
         val glJournal = GeneralLedgerJournalDTO(
            id = null,
            account = AccountDTO(account!!),
            profitCenter = StoreDTO(store!!),
            date = lastDayOfMonth,
            source = GeneralLedgerSourceCodeDTO(source!!),
            amount = accountEntryList.fold(BigDecimal.ZERO) { acc, item -> acc + item.credit + item.debit},
            message = accountEntryList[0].message
         )
         generalLedgerJournalService.create(glJournal, company)

         stagingDepositRepository.updateMovedPendingJE(company, stagingIds)
      } else throw NotFoundException("No Accounting Entries to Post To General Ledger Journal")
   }
}
