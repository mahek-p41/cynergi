package com.cynergisuite.middleware.shipvia.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.error.ErrorValueObject
import com.cynergisuite.middleware.shipvia.ShipViaFactory
import com.cynergisuite.middleware.shipvia.ShipViaFactoryService
import com.cynergisuite.middleware.shipvia.ShipViaValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

import javax.inject.Inject

@MicronautTest(transactional = false)
class ShipViaControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/shipvia"

   @Inject ShipViaFactoryService shipViaFactoryService

   void "fetch one shipVia by id" (){
      given:
      final def shipVia = shipViaFactoryService.single()

      when:
      def result = get("$path/${shipVia.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == shipVia.id

   }

   void "fetch one shipvia by id not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.body().with {parseResponse(it)}
      response.size()== 1
      response.message == "Resource 0 was unable to be found"
   }

   void "post valid shipVia"() {
      given:
      final def shipVia = ShipViaFactory.single().with {new ShipViaValueObject(it)}

      when:
      def response = post("$path/", shipVia)

      then:
      response.id != null
      response.id > 0
      response.name == shipVia.name
      response.description == shipVia.description

   }

   void "post null values to shipVia()"() {
      given:
      final def shipVia = new ShipViaValueObject(null,null, null)

      when:
      post("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      exception.response.body != null

   }

   void "put valid shipVia"() {
      given:
      final def shipVia = shipViaFactoryService.single().with {new ShipViaValueObject(it.id, "test", "test description")}

      when:
      def response = put("$path/", shipVia)

      then:
      response.id != null
      response.id > 0
      response.name == "test"
      response.description == "test description"

   }
}
