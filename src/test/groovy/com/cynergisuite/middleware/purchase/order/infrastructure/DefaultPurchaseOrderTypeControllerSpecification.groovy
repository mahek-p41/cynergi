package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class DefaultPurchaseOrderTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject DefaultPurchaseOrderTypeDataLoaderService dataLoaderService

   void "fetch all default purchase order types" () {
      given:
      def predefinedDefaultPurchaseOrderType = dataLoaderService.predefined().collect { new DefaultPurchaseOrderTypeDTO(it) }

      when:
      def response = get( "/purchase/order/type/default")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new DefaultPurchaseOrderTypeDTO(it.value, it.description) } == predefinedDefaultPurchaseOrderType
   }
}
