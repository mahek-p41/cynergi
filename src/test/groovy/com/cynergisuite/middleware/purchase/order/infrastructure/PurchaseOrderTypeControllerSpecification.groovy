package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTypeFactoryService
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTypeValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class PurchaseOrderTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject PurchaseOrderTypeFactoryService factoryService

   void "fetch all purchase order types" () {
      given:
      def predefinedPurchaseOrderType = factoryService.predefined().collect { new PurchaseOrderTypeValueObject(it) }

      when:
      def response = get( "/purchase/order/type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new PurchaseOrderTypeValueObject(it.value, it.description) } == predefinedPurchaseOrderType
   }
}
