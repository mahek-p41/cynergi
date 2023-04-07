package com.cynergisuite.middleware.accounting.general.ledger.end.year.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountStatusFactory
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.AccountTypeFactory
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarCompleteDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.end.year.EndYearProceduresDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.LocalDate

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class GeneralLedgerProcedureControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/procedure"

   @Inject GeneralLedgerJournalDataLoaderService glJournalDataLoaderService
   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService
   @Inject GeneralLedgerSummaryDataLoaderService generalLedgerSummaryDataLoaderService

   void "end current year with no pending journal entries" () {
      given:
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([year: 2022, periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final capitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value == "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      glJournalDataLoaderService.stream(12, company, capitalAccount, store, LocalDate.now(), glSourceCode).toList()
      glJournalDataLoaderService.stream(1, company2, capitalAccount, store, beginDate.plusMonths(1), glSourceCode).toList()
      generalLedgerSummaryDataLoaderService.single(company, capitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "R"})
      generalLedgerSummaryDataLoaderService.single(company, capitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "P"})
      generalLedgerSummaryDataLoaderService.single(company, capitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "C"})
      generalLedgerSummaryDataLoaderService.single(company, capitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "N"})
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(capitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)
      post("$path/end-year", body)

      then:
      notThrown(HttpClientResponseException)
   }

   void "end current year with pending journal entries and capital account" () {
      given:
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([year: 2022, periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final capitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value == "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      glJournalDataLoaderService.stream(12, company, capitalAccount, store, beginDate.plusMonths(1), glSourceCode).toList()
      glJournalDataLoaderService.stream(1, company2, capitalAccount, store, LocalDate.now(), glSourceCode).toList()
      generalLedgerSummaryDataLoaderService.single(company, capitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "R"})
      generalLedgerSummaryDataLoaderService.single(company, capitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "P"})
      generalLedgerSummaryDataLoaderService.single(company, capitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "C"})
      generalLedgerSummaryDataLoaderService.single(company, capitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "N"})
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(capitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)
      post("$path/end-year", body)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == 'Pending Journal Entries found for General Ledger fiscal year to be closed. All pending journal entries for this date range (2021-11-09 -> 2022-11-08) must be posted before the General Ledger fiscal year can be closed.'
      response[0].code == 'cynergi.validation.pending.jes.found.for.current.year'
   }


   void "end current year with pending journal entries and non capital account" () {
      given:
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([year: 2022, periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final conCapitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value != "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      glJournalDataLoaderService.stream(12, company, conCapitalAccount, store, beginDate.plusMonths(1), glSourceCode).toList()
      glJournalDataLoaderService.stream(1, company2, conCapitalAccount, store, LocalDate.now(), glSourceCode).toList()
      generalLedgerSummaryDataLoaderService.single(company, conCapitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "R"})
      generalLedgerSummaryDataLoaderService.single(company, conCapitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "P"})
      generalLedgerSummaryDataLoaderService.single(company, conCapitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "C"})
      generalLedgerSummaryDataLoaderService.single(company, conCapitalAccount, store, OverallPeriodTypeDataLoader.predefined().find {it.value == "N"})
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(conCapitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)
      post("$path/end-year", body)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response[0].message == 'Pending Journal Entries found for General Ledger fiscal year to be closed. All pending journal entries for this date range (2021-11-09 -> 2022-11-08) must be posted before the General Ledger fiscal year can be closed.'
      response[0].code == 'cynergi.validation.pending.jes.found.for.current.year'
      response[0].path == null
      response[1].message == 'Must be capital account'
      response[1].code == 'cynergi.validation.must.be'
      response[1].path == 'account'
   }

}
