package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject


import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class StoreControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/store"

   @Inject StoreFactoryService storeFactoryService

   void "fetch one store by id" () {
      when:
      def result = get("$path/1")

      then:
      notThrown(HttpClientResponseException)
      result.id == 1
      result.storeNumber == 1
      result.name == "KANSAS CITY"
   }

   void "fetch one store by id not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "0 was unable to be found"
   }

   void "fetch one store from different dataset than one associated with authenticated user" () {
      given:
      def company = companyFactoryService.forDatasetCode('tstds2')
      def store = storeFactoryService.store(3, company)

      when:
      get("$path/${store.myId()}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${store.myId()} was unable to be found"
   }

   void "fetch all stores" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 2
      pageOneResult.elements[0].id == 1
      pageOneResult.elements[0].storeNumber == 1
      pageOneResult.elements[0].name == "KANSAS CITY"
      pageOneResult.elements[1].id == 2
      pageOneResult.elements[1].storeNumber == 3
      pageOneResult.elements[1].name == "INDEPENDENCE"

      when:
      get("${path}${pageTwo}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }
}
