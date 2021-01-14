package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class DefaultPurchaseOrderTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject DefaultPurchaseOrderTypeDataLoaderService dataLoaderService

   void "fetch all default purchase order types" () {
      given:
      def predefinedDefaultPurchaseOrderType = dataLoaderService.predefined().collect { new DefaultPurchaseOrderTypeDTO(it) }

      when:
      def response = get("/purchase-order/type/default")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new DefaultPurchaseOrderTypeDTO(it.value, it.description) } == predefinedDefaultPurchaseOrderType
   }
}
