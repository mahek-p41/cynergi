package com.cynergisuite.middleware.accounting.general.ledger.inquiry.infrastructure

import com.cynergisuite.domain.GeneralLedgerInquiryFilterRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarCompleteDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import java.time.LocalDate

import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerInquiryControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/inquiry"

   @Inject GeneralLedgerSummaryDataLoaderService dataLoaderService
   @Inject AccountTestDataLoaderService accountDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSummary1 = dataLoaderService.single(company, acct, store, OverallPeriodTypeDataLoader.predefined().get(1))
      final glSummary2 = dataLoaderService.single(company, acct, store, OverallPeriodTypeDataLoader.predefined().get(2))
      final beginDate = LocalDate.parse("2021-11-09")
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([year: 2022, periodFrom: beginDate])
      final filterRequest = new GeneralLedgerInquiryFilterRequest([account: acct.number, profitCenter: store.myId(), fiscalYear: 2022])

      when:
      def result2 = post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(Exception)
      result2 != null

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         with(overallPeriod) {
            value == glSummary2.overallPeriod.value
            description == glSummary2.overallPeriod.description
         }
         beginningBalance == glSummary2.beginningBalance
         closingBalance == glSummary2.closingBalance
      }
   }
}
