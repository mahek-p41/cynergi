package com.cynergisuite.middleware.accounting.general.ledger.trial.balance.infrastructure

import com.cynergisuite.domain.GeneralLedgerTrialBalanceReportFilterRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountStatusType
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.AccountTypeFactory
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarGLAPDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDetailDataLoader
import com.cynergisuite.middleware.store.StoreDTO
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.LocalDate

@MicronautTest(transactional = false)
class GeneralLedgerTrialBalanceControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/trial-balance"

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject FinancialCalendarDataLoaderService financialCalendarDataLoaderService
   @Inject GeneralLedgerJournalEntryDataLoaderService generalLedgerJournalEntryDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "filter for trial balance report" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), false, false).collect()
      final periodFrom = LocalDate.now()
      final periodTo = LocalDate.now().plusDays(80)

      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final status = new AccountStatusType(1, 'A', 'Active', 'active')
      final account = accountDataLoaderService.single(company, status, AccountTypeFactory.predefined().find {it.value == "A" })
      final account2 = accountDataLoaderService.single(company, status, AccountTypeFactory.predefined().find {it.value == "E" })
      final account3 = accountDataLoaderService.single(company, status, AccountTypeFactory.predefined().find {it.value == "R" })
      final profitCenter1 = storeFactoryService.store(1, company)
      final profitCenter2 = storeFactoryService.store(3, company)

      def glJournalEntryDetailDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(account), new StoreDTO(profitCenter1), 1000 as BigDecimal).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(account), new StoreDTO(profitCenter1), -1000 as BigDecimal).toList()
      glJournalEntryDetailDTOs.addAll(glJournalEntryDetailCreditDTOs)
      def glJournalEntryDTO = generalLedgerJournalEntryDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, glJournalEntryDetailDTOs, false)

      def glJournalEntryDetailDTOs2 = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(account2), new StoreDTO(profitCenter2), 200 as BigDecimal).toList()
      def glJournalEntryDetailCreditDTOs2 = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(account2), new StoreDTO(profitCenter2), -200 as BigDecimal).toList()
      glJournalEntryDetailDTOs2.addAll(glJournalEntryDetailCreditDTOs2)
      def glJournalEntryDTO2 = generalLedgerJournalEntryDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, glJournalEntryDetailDTOs2, false)

      def glJournalEntryDetailDTOs3 = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(account3), new StoreDTO(profitCenter1), 600 as BigDecimal).toList()
      def glJournalEntryDetailCreditDTOs3 = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(account3), new StoreDTO(profitCenter1), -600 as BigDecimal).toList()
      glJournalEntryDetailDTOs3.addAll(glJournalEntryDetailCreditDTOs3)
      def glJournalEntryDTO3 = generalLedgerJournalEntryDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, glJournalEntryDetailDTOs3, false)


      def filterRequest = new GeneralLedgerTrialBalanceReportFilterRequest()
      filterRequest['from'] = periodFrom
      filterRequest['thru'] = periodFrom.plusDays(30)

      filterRequest['beginAccount'] = account.number
      filterRequest['endAccount'] = account2.number

      final dateRanges = new FinancialCalendarGLAPDateRangeDTO(periodFrom, periodTo, LocalDate.now(), LocalDate.now().plusMonths(1))

      when:
      put("/accounting/financial-calendar/open-gl-ap", dateRanges)

      then:
      notThrown(Exception)

      when: 'create journal entries'
      def result = post("/accounting/general-ledger/journal-entry", glJournalEntryDTO)
      def result2 = post("/accounting/general-ledger/journal-entry", glJournalEntryDTO2)
      def result3 = post("/accounting/general-ledger/journal-entry", glJournalEntryDTO3)

      then:
      notThrown(Exception)
      result != null
      result2 != null

      when: 'fetch report by accounts'
      def response = get("$path/report$filterRequest")

      then:
      notThrown(Exception)
      response != null
      with(response) {
         with(accounts[0]) {
            accountNumber == account.number
            accountType == 'A'
            with(glDetails) {
               profitCenter == [1, 1, 1, 1]
               amount == [1000, 1000, -1000, -1000]
            }
         }
         with(accounts[1]) {
            accountNumber == account2.number
            accountType == 'E'
            with(glDetails) {
               profitCenter == [3, 3, 3, 3]
               amount == [200, 200, -200, -200]
            }
         }
         with(reportGLTotals) {
            debit == 2400
            credit == -2400
            beginBalance == 0
            endBalance == 0
            netChange == 0
            ytdDebit == 2400
            ytdCredit == -2400
         }
         with(endOfReport) {
            mtdDebitIE == 400
            mtdCreditIE == -400
            mtdDifferenceIE == 0
            ytdDebitIE == 400
            ytdCreditIE == -400
            ytdDifferenceIE == 0
            mtdDebitAL == 2000
            mtdCreditAL == -2000
            mtdDifferenceAL == 0
            ytdDebitAL == 2000
            ytdCreditAL == -2000
            ytdDifferenceAL == 0
         }
      }

      when: 'fetch report by profit center'
      filterRequest['beginAccount'] = null
      filterRequest['endAccount'] = null
      filterRequest['profitCenter'] = profitCenter1.myNumber()
      def response2 = get("$path/report$filterRequest")

      then:
      notThrown(Exception)
      response2 != null
      with(response2) {
         with(accounts[0]) {
            accountNumber == account.number
            accountType == 'A'
            with(glDetails) {
               profitCenter == [1, 1, 1, 1]
               amount == [1000, 1000, -1000, -1000]
            }
         }
         with(accounts[1]) {
            accountNumber == account3.number
            accountType == 'R'
            with(glDetails) {
               profitCenter == [1, 1, 1, 1]
               amount == [600, 600, -600, -600]
            }
         }
         with(reportGLTotals) {
            debit == 3200
            credit == -3200
            beginBalance == 0
            endBalance == 0
            netChange == 0
            ytdDebit == 3200
            ytdCredit == -3200
         }
         with(endOfReport) {
            mtdDebitIE == 1200
            mtdCreditIE == -1200
            mtdDifferenceIE == 0
            ytdDebitIE == 1200
            ytdCreditIE == -1200
            ytdDifferenceIE == 0
            mtdDebitAL == 2000
            mtdCreditAL == -2000
            mtdDifferenceAL == 0
            ytdDebitAL == 2000
            ytdCreditAL == -2000
            ytdDifferenceAL == 0
         }
      }
   }
}
