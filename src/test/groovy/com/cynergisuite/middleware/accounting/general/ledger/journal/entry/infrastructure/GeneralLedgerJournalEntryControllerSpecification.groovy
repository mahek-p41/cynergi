package com.cynergisuite.middleware.accounting.general.ledger.journal.entry.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDetailDataLoader
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
         new SimpleIdentifiableDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId()),
         10000 as BigDecimal
      ).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new SimpleIdentifiableDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId()),
         -10000 as BigDecimal
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
         new SimpleIdentifiableDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId()),
         10000 as BigDecimal
      ).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new SimpleIdentifiableDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId()),
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
         new SimpleIdentifiableDTO(account1),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId()),
         10000 as BigDecimal
      ).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new SimpleIdentifiableDTO(account2),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId()),
         -10000 as BigDecimal
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
         new SimpleIdentifiableDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId()),
         10000 as BigDecimal
      ).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(
         2,
         new SimpleIdentifiableDTO(account),
         new SimpleLegacyIdentifiableDTO(profitCenter.myId()),
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
}
