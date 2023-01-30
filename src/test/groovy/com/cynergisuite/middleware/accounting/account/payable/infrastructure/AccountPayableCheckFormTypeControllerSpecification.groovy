package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class AccountPayableCheckFormTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject AccountPayableCheckFormTypeDataLoaderService dataLoaderService

   void "fetch all account payable check form types" () {
      given:
      def predefinedAccountPayableCheckFormType = dataLoaderService.predefined().collect { new AccountPayableCheckFormTypeDTO(it) }

      when:
      def response = get("/accounting/account-payable/type/check-form")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new AccountPayableCheckFormTypeDTO(it.value, it.description) } == predefinedAccountPayableCheckFormType
   }
}
