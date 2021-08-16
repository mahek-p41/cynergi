package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.vendor.rebate.RebateTypeDTO
import com.cynergisuite.middleware.vendor.rebate.RebateTestTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class RebateTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject RebateTestTypeDataLoaderService dataLoaderService

   void "fetch all rebate types" () {
      given:
      def predefinedRebateType = dataLoaderService.predefined().collect { new RebateTypeDTO(it) }

      when:
      def response = get("/vendor/rebate/type")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new RebateTypeDTO(it.value, it.description) } == predefinedRebateType
   }
}
