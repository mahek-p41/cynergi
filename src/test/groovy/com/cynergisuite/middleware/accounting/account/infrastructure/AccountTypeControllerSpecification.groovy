package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTypeFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class AccountTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountTypeFactoryService accountTypeFactoryService

   void "fetch all account types" () {
      given:
      def predefinedAccountStatus = accountTypeFactoryService.predefined()

      when:
      def response = get( "/accounting/account/type")

      then:
      notThrown(HttpClientResponseException)
      response.size() == 2
      response.eachWithIndex { it, index ->
         it.id == predefinedAccountStatus[index].id
         it.value == predefinedAccountStatus[index].value
         it.description == predefinedAccountStatus[index].description
      }
   }
}
