package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeTestDataLoaderService
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class PurchaseOrderTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject PurchaseOrderTypeTestDataLoaderService factoryService

   void "fetch all purchase order types" () {
      given:
      def predefinedPurchaseOrderType = factoryService.predefined().collect { new PurchaseOrderTypeValueObject(it) }

      when:
      def response = get("/purchase-order/type/type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new PurchaseOrderTypeValueObject(it.value, it.description) } == predefinedPurchaseOrderType
   }
}
