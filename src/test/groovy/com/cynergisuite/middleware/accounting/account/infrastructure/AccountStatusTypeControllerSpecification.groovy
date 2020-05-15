package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountStatusFactoryService
import com.cynergisuite.middleware.accounting.account.AccountStatusTypeValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class AccountStatusTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountStatusFactoryService accountStatusFactoryService

   void "fetch all account statuses" () {
      given:
      def predefinedAccountStatus = accountStatusFactoryService.predefined().collect { new AccountStatusTypeValueObject(it) }

      when:
      def response = get( "/accounting/account/status")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountStatusTypeValueObject(it) } == predefinedAccountStatus
   }
}
