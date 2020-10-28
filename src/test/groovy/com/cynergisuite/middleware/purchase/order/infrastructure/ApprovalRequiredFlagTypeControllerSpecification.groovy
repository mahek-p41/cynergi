package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagTypeFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class ApprovalRequiredFlagTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject ApprovalRequiredFlagTypeFactoryService factoryService

   void "fetch all approval required flag types" () {
      given:
      def predefinedApprovalRequiredFlagType = factoryService.predefined().collect { new ApprovalRequiredFlagDTO(it) }

      when:
      def response = get("/purchase/order/type/approval-required-flag")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new ApprovalRequiredFlagDTO(it.value, it.description) } == predefinedApprovalRequiredFlagType
   }
}
