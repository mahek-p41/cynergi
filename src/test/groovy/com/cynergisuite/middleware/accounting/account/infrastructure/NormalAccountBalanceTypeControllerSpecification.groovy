package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.NormalAccountBalanceFactoryService
import com.cynergisuite.middleware.accounting.account.NormalAccountBalanceTypeValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class NormalAccountBalanceTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject NormalAccountBalanceFactoryService normalAccountBalanceFactoryService

   void "fetch all normal account balances" () {
      given:
      def predefinedNormalAccountBalance = normalAccountBalanceFactoryService.predefined().collect { new NormalAccountBalanceTypeValueObject(it) }

      when:
      def response = get("/accounting/account/balance-type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new NormalAccountBalanceTypeValueObject(it) } == predefinedNormalAccountBalance
   }
}
