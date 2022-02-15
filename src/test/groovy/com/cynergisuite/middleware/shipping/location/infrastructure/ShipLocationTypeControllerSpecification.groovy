package com.cynergisuite.middleware.shipping.location.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeDTO
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject

@MicronautTest(transactional = false)
class ShipLocationTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject ShipLocationTypeTestDataLoaderService dataLoaderService

   void "fetch all ship location types" () {
      given:
      def predefinedShipLocationType = dataLoaderService.predefined().collect { new ShipLocationTypeDTO(it) }

      when:
      def response = get("/shipping/location")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new ShipLocationTypeDTO(it.value, it.description) } == predefinedShipLocationType
   }
}
