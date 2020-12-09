package com.cynergisuite.middleware.accounting.general.ledger.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject
import java.time.LocalDate

import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerJournalControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/journal"

   @Inject GeneralLedgerJournalDataLoaderService dataLoaderService
   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final acct = accountDataLoaderService.single(company)
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company)
      final glJournal = dataLoaderService.single(company, acct, store, LocalDate.now(), glSourceCode)

      when:
      def result = get("$path/${glJournal.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glJournal.id
         account.id == glJournal.account.id
         profitCenter.id == glJournal.profitCenter.myId()
         date == glJournal.date.toString()
         source.id == glJournal.source.id
         amount == glJournal.amount
         message == glJournal.message
      }
   }

   void "fetch one not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "fetch all" () {

   }

}
