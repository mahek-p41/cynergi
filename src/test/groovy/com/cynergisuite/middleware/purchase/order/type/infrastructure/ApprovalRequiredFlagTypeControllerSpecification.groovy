package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagTypeTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class ApprovalRequiredFlagTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject ApprovalRequiredFlagTypeTestDataLoaderService factoryService

   void "fetch all approval required flag types" () {
      given:
      def predefinedApprovalRequiredFlagType = factoryService.predefined().collect { new ApprovalRequiredFlagDTO(it) }

      when:
      def response = get("/purchase-order/type/approval-required-flag")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new ApprovalRequiredFlagDTO(it.value, it.description) } == predefinedApprovalRequiredFlagType
   }
}