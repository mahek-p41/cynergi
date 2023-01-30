package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class PrintCurrencyIndicatorTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject PrintCurrencyIndicatorTypeDataLoaderService dataLoaderService

   void "fetch all print currency indicator types" () {
      given:
      def predefinedPrintCurrencyIndicatorType = dataLoaderService.predefined().collect { new PrintCurrencyIndicatorTypeDTO(it) }

      when:
      def response = get("/accounting/account-payable/type/print-currency-indicator")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new PrintCurrencyIndicatorTypeDTO(it.value, it.description) } == predefinedPrintCurrencyIndicatorType
   }
}
