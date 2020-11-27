package com.cynergisuite.middleware.shipping.shipvia.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.error.ErrorDTO
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoader
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class ShipViaControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/shipping/shipvia"

   @Inject ShipViaTestDataLoaderService shipViaFactoryService

   void "fetch one shipVia by id" (){
      given:
      final def shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

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

   void "fetch all" () {
      given:
      def twentyShipVias = shipViaFactoryService.stream(20, nineNineEightEmployee.company).map { new ShipViaDTO(it)}.sorted { o1, o2 -> o1.id <=> o2.id }.toList()
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
      pageOneResult.elements.collect { new ShipViaDTO(it) } == firstPageShipVia

      when:
      def pageTwoResult = get("$path${pageTwo}")

      then:
      pageTwoResult.requested.with { new StandardPageRequest(it) } == pageTwo
      pageTwoResult.totalElements == 20
      pageTwoResult.totalPages == 4
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.collect { new ShipViaDTO(it) } == secondPageShipVia

      when:
      def pageLastResult = get("$path${pageLast}")

      then:
      pageLastResult.requested.with { new StandardPageRequest(it) } == pageLast
      pageLastResult.totalElements == 20
      pageLastResult.totalPages == 4
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 5
      pageLastResult.elements.collect { new ShipViaDTO(it) } == lastPageShipVia

      when:
      get("$path/${pageFive}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all without page" () {
      given:
      def twentyShipVias = shipViaFactoryService.stream(20, nineNineEightEmployee.company).map { new ShipViaDTO(it)}.toList()
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
      pageOneResult.elements.collect { new ShipViaDTO(it) } == firstPageShipVia
   }

   void "fetch all with page" () {
      given:
      def twentyShipVias = shipViaFactoryService.stream(20, nineNineEightEmployee.company).map { new ShipViaDTO(it)}.toList()
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
      pageOneResult.elements.collect { new ShipViaDTO(it) } == firstPageShipVia
   }

   void "post valid shipVia" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final shipVia = ShipViaTestDataLoader.single(company).with { new ShipViaDTO(it) }

      when:
      def response = post("$path/", shipVia)

      then:
      response.id != null
      response.id > 0
      response.description == shipVia.description
      response.number != null
      response.number == response.id
   }

   void "post null values to shipVia" () {
      given:
      final def shipVia = new ShipViaDTO(null as String, 5)

      when:
      post("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 1
      result.collect { new ErrorDTO(it.message, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDTO("Is required", "description")
      ]
   }

   void "put valid shipVia" () {
      given:
      final def shipVia = shipViaFactoryService.single(nineNineEightEmployee.company).with { new ShipViaDTO(it.id, "test description", null) }

      when:
      def response = put("$path/", shipVia)

      then:
      response.id != null
      response.id > 0
      response.description == "test description"
      response.number == shipVia.id
   }

   void "put invalid shipVia" () {
      given:
      final def shipVia = shipViaFactoryService.single(nineNineEightEmployee.company).with {new ShipViaDTO(it.id, null, 5)}

      when:
      put("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 1
      result.collect { new ErrorDTO(it.message, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDTO("Is required", "description")
      ]
   }

   void "put invalid shipVia missing Id" () {
      given:
      final def shipVia = shipViaFactoryService.single(nineNineEightEmployee.company).with {new ShipViaDTO(null, "Gary was here", 5)}

      when:
      put("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND

      def result = exception.response.bodyAsJson()
      new ErrorDTO(result.message, result.path) == new ErrorDTO(" was unable to be found", null)
   }

   void "delete ship via" () {
      given:
      def shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      when:
      delete( "$path/$shipVia.id", )

      then: "ship via of for user's company is delete"
      notThrown(HttpClientResponseException)

      when:
      get("$path/$shipVia.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "$shipVia.id was unable to be found"
   }

   void "delete ship via from other company is not allowed" () {
      given:
      def tstds2 = companies.find { it.datasetCode == "tstds2" }
      shipViaFactoryService.single(nineNineEightEmployee.company)
      def shipVia = shipViaFactoryService.single(tstds2)

      when:
      delete( "$path/$shipVia.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "$shipVia.id was unable to be found"
   }
}
