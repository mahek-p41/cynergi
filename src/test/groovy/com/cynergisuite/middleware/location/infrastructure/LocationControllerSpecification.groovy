package com.cynergisuite.middleware.location.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.location.LocationDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class LocationControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/location"
   @Inject LocationDataLoaderService locationDataLoaderService

   void "fetch one location by id" () {
      given: 'location number 1 is assigned to company tstds1'
      final company = companyFactoryService.forDatasetCode('coravt')
      final loc1 = locationDataLoaderService.location(1, company)

      when:
      def result = get("$path/$loc1.id")

      then:
      notThrown(HttpClientResponseException)
      result.id == loc1.id
      result.locationNumber == loc1.number
      result.name == loc1.name
   }

   void "fetch one location by id not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "0 was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch one location from different dataset than one associated with authenticated user" () {
      given:
      def company = companyFactoryService.forDatasetCode('corrto')
      def location = locationDataLoaderService.location(106, company)

      when:
      get("$path/${location.myId()}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${location.myId()} was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all locations" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.totalElements == 7
      pageOneResult.elements.size() == 5
      pageOneResult.elements[0].id == 1
      pageOneResult.elements[0].locationNumber == 0
      pageOneResult.elements[0].name == "ADVANCED VENTURES,LLC"
      pageOneResult.elements[1].id == 2
      pageOneResult.elements[1].locationNumber == 1
      pageOneResult.elements[1].name == "HOUMA"
      pageOneResult.elements[2].id == 3
      pageOneResult.elements[2].locationNumber == 9000
      pageOneResult.elements[2].name == "ADVANCED VENTURES,LLC"
      pageOneResult.elements[3].id == 4
      pageOneResult.elements[3].locationNumber == 3
      pageOneResult.elements[3].name == "HATTIESBURG"
   }
}
