package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.NormalAccountBalanceFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class NormalAccountBalanceTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject NormalAccountBalanceFactoryService normalAccountBalanceFactoryService

   void "fetch all normal account balances" () {
      given:
      def predefinedNormalAccountBalance = normalAccountBalanceFactoryService.predefined()

      when:
      def response = get( "/accounting/account/balance-type")

      then:
      notThrown(HttpClientResponseException)
      response.size() == 2
      response.eachWithIndex { it, index ->
         it.id == predefinedNormalAccountBalance[index].id
         it.value == predefinedNormalAccountBalance[index].value
         it.description == predefinedNormalAccountBalance[index].description
      }
   }
}
