package com.cynergisuite.middleware.shipvia.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import com.cynergisuite.middleware.shipvia.ShipViaFactoryService
import com.cynergisuite.middleware.shipvia.ShipViaValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject


import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class ShipViaControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/shipvia"

   @Inject ShipViaFactoryService shipViaFactoryService

   void "fetch one shipVia by id" (){
      given:
      final def shipVia = shipViaFactoryService.single(authenticatedEmployee.company)

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
      def response = exception.response.bodyAsJson()
      response.size()== 1
      response.message == "0 was unable to be found"
   }

   void "fetch all"() {
      given:
      def twentyShipVias = shipViaFactoryService.stream(20, authenticatedEmployee.company).map { new ShipViaValueObject(it)}.sorted { o1,o2 -> o1.id <=> o2.id }.toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(4, 5, "id", "ASC")
      def pageFive = new StandardPageRequest(5, 5, "id", "ASC")
      def firstPageShipVia = twentyShipVias[0..4]
      def secondPageShipVia = twentyShipVias[5..9]
      def lastPageShipVia = twentyShipVias[15..19]

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new StandardPageRequest(it) } == pageOne
      pageOneResult.totalElements == 20
      pageOneResult.totalPages == 4
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.collect { new ShipViaValueObject(it) } == firstPageShipVia

      when:
      def pageTwoResult = get("$path${pageTwo}")

      then:
      pageTwoResult.requested.with { new StandardPageRequest(it) } == pageTwo
      pageTwoResult.totalElements == 20
      pageTwoResult.totalPages == 4
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.collect { new ShipViaValueObject(it) } == secondPageShipVia

      when:
      def pageLastResult = get("$path${pageLast}")

      then:
      pageLastResult.requested.with { new StandardPageRequest(it) } == pageLast
      pageLastResult.totalElements == 20
      pageLastResult.totalPages == 4
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 5
      pageLastResult.elements.collect { new ShipViaValueObject(it) } == lastPageShipVia

      when:
      get("$path/${pageFive}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all without page"() {
      given:
      def twentyShipVias = shipViaFactoryService.stream(20, authenticatedEmployee.company).map { new ShipViaValueObject(it)}.toList()
      def firstPageShipVia = twentyShipVias[0..9]

      when:
      def pageOneResult = get(path)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.with { new StandardPageRequest(it) } == new StandardPageRequest(null)
      pageOneResult.totalElements == 20
      pageOneResult.totalPages == 2
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 10
      pageOneResult.elements.collect { new ShipViaValueObject(it) } == firstPageShipVia
   }

   void "fetch all with page"() {
      given:
      def twentyShipVias = shipViaFactoryService.stream(20, authenticatedEmployee.company).map { new ShipViaValueObject(it)}.toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def firstPageShipVia = twentyShipVias[0..4]

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new StandardPageRequest(it) } == pageOne
      pageOneResult.totalElements == 20
      pageOneResult.totalPages == 4
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.collect { new ShipViaValueObject(it) } == firstPageShipVia
   }

   void "post valid shipVia"() {
      given:
      final def shipVia = shipViaFactoryService.single(authenticatedEmployee.company).with { new ShipViaValueObject(it) }

      when:
      def response = post("$path/", shipVia)

      then:
      response.id != null
      response.id > 0
      response.description == shipVia.description
   }

   void "post null values to shipVia()"() {
      given:
      final def shipVia = new ShipViaValueObject(null as String)

      when:
      post("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 1
      result.collect { new ErrorDataTransferObject(it.message, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Is required", "description")
      ]
   }

   void "put valid shipVia"() {
      given:
      final def shipVia = shipViaFactoryService.single(authenticatedEmployee.company).with {new ShipViaValueObject(it.id, "test description")}

      when:
      def response = put("$path/", shipVia)

      then:
      response.id != null
      response.id > 0
      response.description == "test description"
   }

   void "put invalid shipVia"(){
      given:
      final def shipVia = shipViaFactoryService.single(authenticatedEmployee.company).with {new ShipViaValueObject(it.id, null)}

      when:
      put("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 1
      result.collect { new ErrorDataTransferObject(it.message, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Is required", "description")
      ]
   }

   void "put invalid shipVia missing Id"(){
      given:
      final def shipVia = shipViaFactoryService.single(authenticatedEmployee.company).with {new ShipViaValueObject(null, "Gary was here")}

      when:
      put("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 1
      result.collect { new ErrorDataTransferObject(it.message, it.path) } == [
         new ErrorDataTransferObject("Is required", "id")
      ]
   }
}
