package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerSpecificationBase
import com.hightouchinc.cynergi.middleware.entity.AreaDto
import com.hightouchinc.cynergi.test.data.loader.AreaDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.hateos.JsonError

import static com.hightouchinc.cynergi.test.helper.SpecificationHelpers.allPropertiesFullAndNotEmpty
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.NOT_FOUND

class AreaControllerSpecification extends ControllerSpecificationBase {
   final def url = "/api/area"
   final def areaDataLoaderService = applicationContext.getBean(AreaDataLoaderService)

   void "fetch one area by id" () {
      given:
      final def savedArea = areasDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Area") }
      final def areaDto = new AreaDto(savedArea)
      
      when:
      def result = client.retrieve(GET("$url/${savedArea.id}"), AreaDto)
      
      then:
      result == areaDto
      allPropertiesFullAndNotEmpty(result)
      // TODO more testing of the result
   }

   void "fetch one area by id not found" () {
      when:
      client.exchange(GET("$url/0"), AreaDto)

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(JsonError).orElse(null)?.message == "Resource 0 was unable to be found"
   }
}
