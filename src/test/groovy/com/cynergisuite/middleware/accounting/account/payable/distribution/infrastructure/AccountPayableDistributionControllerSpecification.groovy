package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class AccountPayableDistributionControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/accounting/account-payable/distribution'

   @Inject AccountPayableDistributionDataLoaderService dataLoaderService
   @Inject AccountDataLoaderService accountDataLoaderService

   void "fetch one account payable distribution by id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final acct = accountDataLoaderService.single(company)
      final def apDist = dataLoaderService.single(store, acct, company)

      when:
      def result = get("$path/${apDist.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == apDist.id
         name == apDist.name
         profitCenter.id == apDist.profitCenter.id
         account.id == apDist.account.id
         percent == apDist.percent
      }
   }

}
