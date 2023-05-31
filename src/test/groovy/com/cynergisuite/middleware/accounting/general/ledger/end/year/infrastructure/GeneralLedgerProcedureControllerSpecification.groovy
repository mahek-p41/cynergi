package com.cynergisuite.middleware.accounting.general.ledger.end.year.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountStatusFactory
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.AccountTypeFactory
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarCompleteDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarGLAPDateRangeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.end.year.EndYearProceduresDTO
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDetailDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDataLoaderService
import com.cynergisuite.middleware.store.StoreDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class GeneralLedgerProcedureControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/procedure"

   @Inject GeneralLedgerJournalDataLoaderService glJournalDataLoaderService
   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService
   @Inject GeneralLedgerJournalEntryDataLoaderService generalLedgerJournalEntryDataLoaderService
   @Inject GeneralLedgerReversalDataLoaderService generalLedgerReversalDataLoaderService

   void "end current year with no pending journal entries" () {
      given:
      final beginDate = LocalDate.now().minusMonths(26)
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final capitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value == "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(capitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))
      final dateRanges = new FinancialCalendarGLAPDateRangeDTO(LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(2), LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1))

      def glJournalEntryDetailDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(capitalAccount), new StoreDTO(store), 1000 as BigDecimal).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(capitalAccount), new StoreDTO(store), -1000 as BigDecimal).toList()
      glJournalEntryDetailDTOs.addAll(glJournalEntryDetailCreditDTOs)
      def glJournalEntryDTO = generalLedgerJournalEntryDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, glJournalEntryDetailDTOs, false)
      generalLedgerReversalDataLoaderService.single(company, glSourceCode, beginDate)

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(HttpClientResponseException)

      when:
      put("/accounting/financial-calendar/open-gl-ap", dateRanges)

      then:
      notThrown(Exception)

      when: 'create journal entries'
      def result = post("/accounting/general-ledger/journal-entry", glJournalEntryDTO)

      then:
      notThrown(Exception)
      result != null

      when:
      post("$path/end-year", body)

      then:
      notThrown(HttpClientResponseException)
   }

   void "end current year with pending journal entries, unbalance GL, and capital account" () {
      given:
      final beginDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(26)
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final capitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value == "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      glJournalDataLoaderService.stream(12, company, capitalAccount, store, beginDate.plusYears(2).plusMonths(1), glSourceCode).toList()
      glJournalDataLoaderService.stream(1, company2, capitalAccount, store, LocalDate.now(), glSourceCode).toList()
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(capitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))
      final dateRanges = new FinancialCalendarGLAPDateRangeDTO(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(2), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(2), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1))
      generalLedgerReversalDataLoaderService.single(company, glSourceCode, beginDate)

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(HttpClientResponseException)

      when:
      put("/accounting/financial-calendar/open-gl-ap", dateRanges)

      then:
      notThrown(Exception)

      when:
      post("$path/end-year", body)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response[0].message == "Pending Journal Entries found for General Ledger fiscal year to be closed. All pending journal entries for this date range (${beginDate.plusYears(2)} -> ${beginDate.plusYears(3).minusDays(1)}) must be posted before the General Ledger fiscal year can be closed."
      response[0].code == 'cynergi.validation.pending.jes.found.for.current.year'
      response[1].message == 'GL is NOT in Balance'
      response[1].code == 'cynergi.validation.gl.not.in.balance'
   }

   void "end current year with pending journal entries, reversal entries, unbalance GL, and non-capital account" () {
      given:
      final beginDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(26)
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final nonCapitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value != "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      glJournalDataLoaderService.stream(12, company, nonCapitalAccount, store, beginDate.plusYears(2).plusMonths(1), glSourceCode).toList()
      glJournalDataLoaderService.stream(1, company2, nonCapitalAccount, store, LocalDate.now(), glSourceCode).toList()
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(nonCapitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))
      final dateRanges = new FinancialCalendarGLAPDateRangeDTO(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(2), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(2), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1))
      generalLedgerReversalDataLoaderService.single(company, glSourceCode, LocalDate.now())

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(HttpClientResponseException)

      when:
      put("/accounting/financial-calendar/open-gl-ap", dateRanges)

      then:
      notThrown(Exception)

      when:
      post("$path/end-year", body)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 4
      response[0].message == "Pending Journal Entries found for General Ledger fiscal year to be closed. All pending journal entries for this date range (${beginDate.plusYears(2)} -> ${beginDate.plusYears(3).minusDays(1)}) must be posted before the General Ledger fiscal year can be closed."
      response[0].code == 'cynergi.validation.pending.jes.found.for.current.year'
      response[1].message == "Reversal Journal Entries found for General Ledger fiscal year to be closed. All Reversal Journal Entries for this date range must be posted before the General Ledger fiscal year can be closed."
      response[1].code == 'cynergi.validation.unposted.reversals.found.for.current.year'
      response[2].message == 'Must be capital account'
      response[2].code == 'cynergi.validation.must.be'
      response[2].path == 'account'
      response[3].message == 'GL is NOT in Balance'
      response[3].code == 'cynergi.validation.gl.not.in.balance'
   }

}
