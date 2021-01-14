package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDataLoaderService
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class ExceptionIndicatorTypeControllerSpecification extends ControllerSpecificationBase {

   @Inject ExceptionIndicatorTypeDataLoaderService dataLoaderService

   void "fetch all exception indicator types" () {
      given:
      def predefinedExceptionIndicatorType = dataLoaderService.predefined().collect { new ExceptionIndicatorTypeDTO(it) }

      when:
      def response = get("/purchase-order/type/exception-indicator")

      then:
      notThrown(HttpClientResponseException)
      response.collect { new ExceptionIndicatorTypeDTO(it.value, it.description) } == predefinedExceptionIndicatorType
   }
}
