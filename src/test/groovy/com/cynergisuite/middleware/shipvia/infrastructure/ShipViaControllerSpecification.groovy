package com.cynergisuite.middleware.shipvia.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.error.ErrorValueObject
import com.cynergisuite.middleware.shipvia.ShipViaFactory
import com.cynergisuite.middleware.shipvia.ShipViaFactoryService
import com.cynergisuite.middleware.shipvia.ShipViaValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import java.util.stream.Collectors

import static com.cynergisuite.domain.PageRequestSortDirection.ASCENDING
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

   void "fetch all"() {
      given:
      def twentyShipVias = shipViaFactoryService.stream(20 ).map { new ShipViaValueObject(it)}.sorted { o1,o2 -> o1.name <=> o2.name }.collect(Collectors.toList())
      def pageOne = new PageRequest(1, 5, "name", ASCENDING)
      def pageTwo = new PageRequest(2, 5, "name", ASCENDING)
      def pageLast = new PageRequest(4, 5, "name", ASCENDING)
      def pageFive = new PageRequest(5, 5, "name", ASCENDING)
      def firstPageShipVia = twentyShipVias[0..4]
      def secondPageShipVia = twentyShipVias[5..9]
      def lastPageShipVia = twentyShipVias[15..19]

      when:
      def pageOneResult = get("$path/${pageOne}")

      then:
      pageOneResult.requested.with { new PageRequest(it) } == pageOne
      pageOneResult.totalElements == 20
      pageOneResult.totalPages == 4
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.collect { new ShipViaValueObject(it) }.containsAll(firstPageShipVia)

      when:
      def pageTwoResult = get("$path/${pageTwo}")

      then:
      pageTwoResult.requested.with { new PageRequest(it) } == pageTwo
      pageTwoResult.totalElements == 20
      pageTwoResult.totalPages == 4
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.collect { new ShipViaValueObject(it) }.containsAll(secondPageShipVia)

      when:
      def pageLastResult = get("$path/${pageLast}")

      then:
      pageLastResult.requested.with { new PageRequest(it) } == pageLast
      pageLastResult.totalElements == 20
      pageLastResult.totalPages == 4
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 5
      pageLastResult.elements.collect { new ShipViaValueObject(it) }.containsAll(lastPageShipVia)

      when:
      get("$path/${pageFive}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NOT_FOUND
      final def notFoundResult = notFoundException.response.bodyAsJson()
      notFoundResult.size() == 1
      notFoundResult.message == "Request  with Page 5, Size 5, Sort By name and Sort Direction ASC produced no results"

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

      def result = exception.response.bodyAsJson()
      result.size() == 2
      result.collect { new ErrorValueObject(it)}.containsAll([
         new ErrorValueObject("description is required", "description"),
         new ErrorValueObject("name is required", "name")
      ])

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

   void "put invalid shipVia"(){
      given:
      final def shipVia = shipViaFactoryService.single().with {new ShipViaValueObject(it.id, null, null)}

      when:
      put("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 2
      result.collect { new ErrorValueObject(it)}.containsAll([
         new ErrorValueObject("description is required", "description"),
         new ErrorValueObject("name is required", "name")
      ])

   }

   void "put invalid shipVia missing Id"(){
      given:
      final def shipVia = shipViaFactoryService.single().with {new ShipViaValueObject(null, "test", "Gary was here")}

      when:
      put("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 1
      result.collect { new ErrorValueObject(it)}.containsAll([
         new ErrorValueObject("id is required", "id")
      ])

   }
}
