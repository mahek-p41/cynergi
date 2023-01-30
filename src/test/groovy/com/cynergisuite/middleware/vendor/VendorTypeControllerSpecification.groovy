package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class VendorTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject VendorTypeFactoryService vendorTypeFactoryService

   void "fetch all account types" () {
      given:
      def predefinedVendorStatus = vendorTypeFactoryService.predefined().collect { new VendorTypeDTO(it) }

      when:
      def response = get("/vendor/type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new VendorTypeDTO(it) } == predefinedVendorStatus
   }
}
