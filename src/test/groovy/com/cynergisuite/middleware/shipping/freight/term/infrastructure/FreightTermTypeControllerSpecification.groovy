package com.cynergisuite.middleware.shipping.freight.term.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeDTO
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class FreightTermTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject FreightTermTypeDataLoaderService dataLoaderService

   void "fetch all freight term types" () {
      given:
      def predefinedFreightTermType = dataLoaderService.predefined().collect { new FreightTermTypeDTO(it) }

      when:
      def response = get( "/shipping/freight/term")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new FreightTermTypeDTO(it.value, it.description) } == predefinedFreightTermType
   }
}
