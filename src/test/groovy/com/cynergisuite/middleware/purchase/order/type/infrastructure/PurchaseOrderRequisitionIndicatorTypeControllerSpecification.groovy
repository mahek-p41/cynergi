package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderRequisitionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderRequisitionIndicatorTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class PurchaseOrderRequisitionIndicatorTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject PurchaseOrderRequisitionIndicatorTypeDataLoaderService dataLoaderService

   void "fetch all purchase order requisition indicator types" () {
      given:
      def predefinedPurchaseOrderRequisitionIndicatorType = dataLoaderService.predefined().collect { new PurchaseOrderRequisitionIndicatorTypeDTO(it) }

      when:
      def response = get("/purchase/order/type/requisition-indicator")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new PurchaseOrderRequisitionIndicatorTypeDTO(it.value, it.description) } == predefinedPurchaseOrderRequisitionIndicatorType
   }
}
