package com.cynergisuite.middleware.accounting.routine.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class OverallPeriodTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject OverallPeriodTypeDataLoaderService dataLoaderService

   void "fetch all overall period types" () {
      given:
      def predefinedOverallPeriodType = dataLoaderService.predefined().collect { new OverallPeriodTypeDTO(it) }

      when:
      def response = get("/accounting/routine/type/overall-period")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new OverallPeriodTypeDTO(it.value, it.abbreviation, it.description) } == predefinedOverallPeriodType
   }
}
