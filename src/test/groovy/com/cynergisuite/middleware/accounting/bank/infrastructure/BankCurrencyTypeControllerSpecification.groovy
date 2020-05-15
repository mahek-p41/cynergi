package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.bank.BankCurrencyTypeValueObject
import com.cynergisuite.middleware.accounting.bank.CurrencyFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class BankCurrencyTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject CurrencyFactoryService currencyFactoryService

   void "fetch all bank currencies" () {
      given:
      def predefinedCurrencies = currencyFactoryService.predefined().collect { new BankCurrencyTypeValueObject(it) }

      when:
      def response = get( "/accounting/currency")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new BankCurrencyTypeValueObject(it) } == predefinedCurrencies
   }
}
