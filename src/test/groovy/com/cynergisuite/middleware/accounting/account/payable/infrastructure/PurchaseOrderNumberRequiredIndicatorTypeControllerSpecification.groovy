package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class PurchaseOrderNumberRequiredIndicatorTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject PurchaseOrderNumberRequiredIndicatorTypeDataLoaderService dataLoaderService

   void "fetch all purchase order number required indicator types" () {
      given:
      def predefinedPurchaseOrderNumberRequiredIndicatorType = dataLoaderService.predefined().collect { new PurchaseOrderNumberRequiredIndicatorTypeDTO(it) }

      when:
      def response = get("/accounting/account-payable/type/purchase-order-number-required-indicator")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new PurchaseOrderNumberRequiredIndicatorTypeDTO(it.value, it.description) } == predefinedPurchaseOrderNumberRequiredIndicatorType
   }
}
