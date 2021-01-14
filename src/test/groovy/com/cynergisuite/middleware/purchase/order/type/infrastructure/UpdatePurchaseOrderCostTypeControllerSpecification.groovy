package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeFactoryService
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class UpdatePurchaseOrderCostTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject UpdatePurchaseOrderCostTypeFactoryService factoryService

   void "fetch all update purchase order cost types" () {
      given:
      def predefinedUpdatePurchaseOrderCostType = factoryService.predefined().collect { new UpdatePurchaseOrderCostTypeValueObject(it) }

      when:
      def response = get("/purchase-order/type/cost")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new UpdatePurchaseOrderCostTypeValueObject(it.value, it.description) } == predefinedUpdatePurchaseOrderCostType
   }
}
