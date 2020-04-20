package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.bank.CurrencyFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class BankCurrencyTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject CurrencyFactoryService currencyFactoryService

   void "fetch all bank currencies" () {
      given:
      def predefinedCurrencies = currencyFactoryService.predefined()

      when:
      def response = get( "/accounting/currency")

      then:
      notThrown(HttpClientResponseException)
      response.size() == 2
      response.eachWithIndex { it, index ->
         it.id == predefinedCurrencies[index].id
         it.value == predefinedCurrencies[index].value
         it.description == predefinedCurrencies[index].description
      }
   }
}
