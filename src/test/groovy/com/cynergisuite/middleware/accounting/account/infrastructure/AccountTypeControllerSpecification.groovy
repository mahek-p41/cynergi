package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTypeFactoryService
import com.cynergisuite.middleware.accounting.account.AccountTypeValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject

@MicronautTest(transactional = false)
class AccountTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountTypeFactoryService accountTypeFactoryService

   void "fetch all account types" () {
      given:
      def predefinedAccountStatus = accountTypeFactoryService.predefined().collect { new AccountTypeValueObject(it) }

      when:
      def response = get("/accounting/account/type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountTypeValueObject(it) } == predefinedAccountStatus
   }
}
