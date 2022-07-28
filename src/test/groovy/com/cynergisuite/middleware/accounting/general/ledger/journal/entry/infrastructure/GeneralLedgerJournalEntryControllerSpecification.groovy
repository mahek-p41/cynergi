package com.cynergisuite.middleware.accounting.general.ledger.journal.entry.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDetailDataLoader
import com.cynergisuite.middleware.store.StoreDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class GeneralLedgerJournalEntryControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/general-ledger/journal-entry"

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject FinancialCalendarDataLoaderService financialCalendarDataLoaderService
   @Inject GeneralLedgerJournalEntryDataLoaderService dataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "create valid journal entry without reversal" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now()).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusDays(80))

      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glJournalEntryDetailDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account),
         new StoreDTO(profitCenter),
         99999999999.99 as BigDecimal
      ).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account),
         new StoreDTO(profitCenter),
         -99999999999.99 as BigDecimal
      ).toList()
      glJournalEntryDetailDTOs.addAll(glJournalEntryDetailCreditDTOs)
      def glJournalEntryDTO = dataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, glJournalEntryDetailDTOs, false)

      when: 'open GL in financial calendar'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

      when: 'create journal entry'
      def result = post(path, glJournalEntryDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         entryDate == glJournalEntryDTO.entryDate.toString()

         with(source) {
            value == glJournalEntryDTO.source.value
            description == glJournalEntryDTO.source.description
         }

         reverse == glJournalEntryDTO.reverse
         journalEntryNumber != null
         journalEntryNumber > 0

         journalEntryDetails.eachWithIndex { journalEntryDetail, int i ->
            glJournalEntryDetailDTOs.find { element -> element == journalEntryDetail }
         }

         message == glJournalEntryDTO.message
         postReversingEntry == glJournalEntryDTO.postReversingEntry
      }
   }

   void "create valid journal entry with reversal but without posting reversal" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now()).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusDays(80))

      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glJournalEntryDetailDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account),
         new StoreDTO(profitCenter),
         10000 as BigDecimal
      ).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account),
         new StoreDTO(profitCenter),
         -10000 as BigDecimal
      ).toList()
      glJournalEntryDetailDTOs.addAll(glJournalEntryDetailCreditDTOs)
      def glJournalEntryDTO = dataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), true, glJournalEntryDetailDTOs, false)

      when: 'open GL in financial calendar'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

      when: 'create journal entry'
      def result = post(path, glJournalEntryDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         entryDate == glJournalEntryDTO.entryDate.toString()

         with(source) {
            value == glJournalEntryDTO.source.value
            description == glJournalEntryDTO.source.description
         }

         reverse == glJournalEntryDTO.reverse
         journalEntryNumber != null
         journalEntryNumber > 0

         journalEntryDetails.eachWithIndex { journalEntryDetail, int i ->
            glJournalEntryDetailDTOs.find { element -> element == journalEntryDetail }
         }

         message == glJournalEntryDTO.message
         postReversingEntry == glJournalEntryDTO.postReversingEntry
      }
   }

   void "create valid journal entry with posting reversal" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now()).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusDays(80))

      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account1 = accountDataLoaderService.single(company)
      final account2 = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glJournalEntryDetailDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account1),
         new StoreDTO(profitCenter),
         99999999999.99 as BigDecimal
      ).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account2),
         new StoreDTO(profitCenter),
         -99999999999.99 as BigDecimal
      ).toList()
      glJournalEntryDetailDTOs.addAll(glJournalEntryDetailCreditDTOs)
      def glJournalEntryDTO = dataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), true, glJournalEntryDetailDTOs, true)

      when: 'create journal entry'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)
      def result = post(path, glJournalEntryDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         entryDate == glJournalEntryDTO.entryDate.toString()

         with(source) {
            value == glJournalEntryDTO.source.value
            description == glJournalEntryDTO.source.description
         }

         reverse == glJournalEntryDTO.reverse
         journalEntryNumber != null
         journalEntryNumber > 0

         journalEntryDetails.eachWithIndex { journalEntryDetail, int i ->
            glJournalEntryDetailDTOs.find { element -> element == journalEntryDetail }
         }

         message == glJournalEntryDTO.message
         postReversingEntry == glJournalEntryDTO.postReversingEntry
      }
   }

   @Unroll
   void "create invalid journal entry without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now()).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusDays(80))

      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account = accountDataLoaderService.single(company)
      final profitCenter = storeFactoryService.store(3, company)
      def glJournalEntryDetailDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account),
         new StoreDTO(profitCenter),
         10000 as BigDecimal
      ).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account),
         new StoreDTO(profitCenter),
         -10000 as BigDecimal
      ).toList()
      glJournalEntryDetailDTOs.addAll(glJournalEntryDetailCreditDTOs)
      def glJournalEntryDTO = dataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, glJournalEntryDetailDTOs, false)
      glJournalEntryDTO["$nonNullableProp"] = null

      when: 'open GL in financial calendar'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

      when: 'create journal entry'
      post(path, glJournalEntryDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp         || errorResponsePath
      'entryDate'             || 'entryDate'
      'reverse'               || 'reverse'
      'source'                || 'source'
   }

   void "create multiple journal entries" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now()).collect()
      final dateRangeDTO = new FinancialCalendarDateRangeDTO(LocalDate.now(), LocalDate.now().plusDays(80))

      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final account1 = accountDataLoaderService.single(company)
      final account2 = accountDataLoaderService.single(company)
      final profitCenter1 = storeFactoryService.store(1, company)
      final profitCenter2 = storeFactoryService.store(3, company)

      def firstGLJournalEntryDetailDebitDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account1),
         new StoreDTO(profitCenter2),
         10000 as BigDecimal
      ).toList()
      def firstGLJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new AccountDTO(account2),
         new StoreDTO(profitCenter1),
         -10000 as BigDecimal
      ).toList()
      firstGLJournalEntryDetailDebitDTOs.addAll(firstGLJournalEntryDetailCreditDTOs)
      def firstGLJournalEntryDTO = dataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, firstGLJournalEntryDetailDebitDTOs, false)

      def secondGLJournalEntryDetailDebitDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         3,
         new AccountDTO(account2),
         new StoreDTO(profitCenter1),
         99000 as BigDecimal
      ).toList()
      def secondGLJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         3,
         new AccountDTO(account1),
         new StoreDTO(profitCenter2),
         -99000 as BigDecimal
      ).toList()
      secondGLJournalEntryDetailDebitDTOs.addAll(secondGLJournalEntryDetailCreditDTOs)
      def secondGLJournalEntryDTO = dataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, secondGLJournalEntryDetailDebitDTOs, false)

      when: 'open GL in financial calendar'
      put("/accounting/financial-calendar/open-gl", dateRangeDTO)

      then:
      notThrown(Exception)

      when: 'create first journal entry'
      def result = post(path, firstGLJournalEntryDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         entryDate == firstGLJournalEntryDTO.entryDate.toString()

         with(source) {
            value == firstGLJournalEntryDTO.source.value
            description == firstGLJournalEntryDTO.source.description
         }

         reverse == firstGLJournalEntryDTO.reverse
         journalEntryNumber == 1

         journalEntryDetails.eachWithIndex { journalEntryDetail, int i ->
            firstGLJournalEntryDetailDebitDTOs.find { element -> element == journalEntryDetail }
         }

         message == firstGLJournalEntryDTO.message
         postReversingEntry == firstGLJournalEntryDTO.postReversingEntry
      }

      when: 'create multiple journal entries'
      post(path, secondGLJournalEntryDTO)
      result = post(path, secondGLJournalEntryDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         entryDate == secondGLJournalEntryDTO.entryDate.toString()

         with(source) {
            value == secondGLJournalEntryDTO.source.value
            description == secondGLJournalEntryDTO.source.description
         }

         reverse == secondGLJournalEntryDTO.reverse
         journalEntryNumber > 1

         journalEntryDetails.eachWithIndex { journalEntryDetail, int i ->
            secondGLJournalEntryDetailDebitDTOs.find { element -> element == journalEntryDetail }
         }

         message == secondGLJournalEntryDTO.message
         postReversingEntry == secondGLJournalEntryDTO.postReversingEntry
      }
   }
}
