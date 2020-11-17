package com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDataLoaderService
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject
import java.time.LocalDate

@MicronautTest(transactional = false)
class BankReconciliationControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/bank-recon'
   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject BankReconciliationDataLoaderService dataLoaderService
   @Inject BankReconciliationTypeDataLoaderService bankReconciliationTypeDataLoaderService

   void "fetch one bank reconciliation by id" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final account = accountDataLoaderService.single(nineNineEightEmployee.company)
      final store = storeFactoryService.store(3, nineNineEightEmployee.company)
      final bank1 = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      final type = bankReconciliationTypeDataLoaderService.random()
      final bankRecon = dataLoaderService.single(tstds1, bank1, type, LocalDate.now(), null)

      when:
      def result = get("$path/${bankRecon.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == bankRecon.id
         company == bankRecon.company
         bank.id == bankRecon.bank.id

         with(type) {
            id == bankRecon.type.id
            value == bankRecon.type.value
            description == bankRecon.type.description
            localizationCode == bankRecon.type.localizationCode
         }

         date == bankRecon.date
         clearedDate == bankRecon.clearedDate
         amount == bankRecon.amount
         description == bankRecon.description
         document == bankRecon.document
      }
   }

}
