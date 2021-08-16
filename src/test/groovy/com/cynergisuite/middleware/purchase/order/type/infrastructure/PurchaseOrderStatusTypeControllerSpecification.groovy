package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeTestDataLoaderService
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class PurchaseOrderStatusTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject PurchaseOrderStatusTypeTestDataLoaderService testDataLoaderService

   void "fetch all purchase order status types" () {
      given:
      def predefinedPurchaseOrderStatusType = testDataLoaderService.predefined().collect { new PurchaseOrderStatusTypeValueObject(it) }

      when:
      def response = get("/purchase-order/type/status")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new PurchaseOrderStatusTypeValueObject(it.value, it.description) } == predefinedPurchaseOrderStatusType
   }
}
