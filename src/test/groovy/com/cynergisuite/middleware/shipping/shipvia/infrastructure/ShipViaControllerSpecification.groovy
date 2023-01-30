package com.cynergisuite.middleware.shipping.shipvia.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.error.ErrorDTO
import com.cynergisuite.middleware.shipping.shipvia.ShipViaDTO
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoader
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.CONFLICT
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class ShipViaControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/shipping/shipvia"

   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService

   void "fetch one shipVia by id" (){
      given:
      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      when:
      def result = get("$path/${shipVia.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == shipVia.id
   }

   void "fetch one shipvia by id not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("$path/$nonExistentId")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
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
      final notFoundException = thrown(HttpClientResponseException)
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = ShipViaTestDataLoader.single(company).with { new ShipViaDTO(it) }

      when:
      def response = post("$path/", shipVia)

      then:
      response.id != null
      response.description == shipVia.description
      response.number > 0
   }

   void "post null values to shipVia" () {
      given:
      final shipVia = new ShipViaDTO(null as String, 5)

      when:
      post("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 1
      result.collect { new ErrorDTO(it.message, it.code, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "description")
      ]
   }

   void "put valid shipVia" () {
      given:
      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company).with { new ShipViaDTO(it.id, "test description", it.number) }

      when:
      def response = put("$path/", shipVia)

      then:
      response.id != null
      response.description == "test description"
      response.number == shipVia.number
   }

   void "put invalid shipVia" () {
      given:
      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company).with {new ShipViaDTO(it.id, null, 5)}

      when:
      put("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      def result = exception.response.bodyAsJson()
      result.size() == 1
      result.collect { new ErrorDTO(it.message, it.code, it.path) }.sort { o1, o2 -> o1 <=> o2 } == [
         new ErrorDTO("Is required", "javax.validation.constraints.NotNull.message", "description")
      ]
   }

   void "put invalid shipVia missing Id" () {
      given:
      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company).with {new ShipViaDTO(null, "Gary was here", 5)}

      when:
      put("$path/", shipVia)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND

      def result = exception.response.bodyAsJson()
      new ErrorDTO(result.message, result.code, result.path) == new ErrorDTO(" was unable to be found", 'system.not.found', null)
   }

   void "delete ship via" () {
      given:
      def shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      when:
      delete("$path/$shipVia.id")

      then: "ship via of for user's company is deleted"
      notThrown(HttpClientResponseException)

      when:
      get("$path/$shipVia.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$shipVia.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete ship via still has references" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      when:
      delete("$path/$shipVia.id")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == CONFLICT
      def response = exception.response.bodyAsJson()
      response.message == "Requested operation violates data integrity"
      response.code == "cynergi.data.constraint.violated"
   }

   void "delete ship via from other company is not allowed" () {
      given:
      def tstds2 = companies.find { it.datasetCode == "corrto" }
      shipViaFactoryService.single(nineNineEightEmployee.company)
      def shipVia = shipViaFactoryService.single(tstds2)

      when:
      delete( "$path/$shipVia.id")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$shipVia.id was unable to be found"
      response.code == 'system.not.found'

      when:
      delete( "$path/905545bf-3509-4ad3-8ccc-e437b2dbdcb0")

      then:
      final exception2 = thrown(HttpClientResponseException)
      exception2.response.status == NOT_FOUND
      def response2 = exception.response.bodyAsJson()
      response2.message == "$shipVia.id was unable to be found"
      response2.code == 'system.not.found'
   }

   void "delete non-existing ship via" () {
      given:
      def shipVia = shipViaFactoryService.single(nineNineEightEmployee.company)

      when: // delete ship via
      delete("$path/$shipVia.id")

      then:
      notThrown(HttpClientResponseException)

      when: // delete ship via that has already been deleted
      delete("$path/$shipVia.id")

      then:
      final exception1 = thrown(HttpClientResponseException)
      exception1.response.status == NOT_FOUND
      def response1 = exception1.response.bodyAsJson()
      response1.message == "$shipVia.id was unable to be found"
      response1.code == 'system.not.found'

      when: // delete ship via with non-existing uuid
      delete( "$path/905545bf-3509-4ad3-8ccc-e437b2dbdcb0")

      then:
      final exception2 = thrown(HttpClientResponseException)
      exception2.response.status == NOT_FOUND
      def response2 = exception2.response.bodyAsJson()
      response2.message == "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      response2.code == 'system.not.found'
   }

   void "recreate deleted ship via" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = ShipViaTestDataLoader.single(company).with { new ShipViaDTO(it) }

      when: // create a ship via
      def response1 = post("$path/", shipVia)

      then:
      response1.id != null
      response1.description == shipVia.description
      response1.number > 0

      when: // delete ship via
      delete("$path/$response1.id")

      then: "ship via of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate ship via
      def response2 = post("$path/", shipVia)

      then:
      response2.id != null
      response2.description == shipVia.description
      response2.number > 0

      when: // delete ship via again
      delete("$path/$response2.id")

      then: "ship via of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
