package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class StoreControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/store"

   void "fetch one store by id with the a region assigned" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store1 = storeFactoryService.store(1, company)

      when:
      def result = get("$path/$store1.id")

      then: 'store should have a assigned region'
      notThrown(HttpClientResponseException)
      result.id == store1.id
      result.storeNumber == store1.number
      result.name == store1.name
      result.region.name == regions[0].name
      result.region.division.name == divisions[0].name
   }

   void "fetch one store by id without region assigned" () {
      given: 'store store3Tstds1 is not assigned to any region, store store3Tstds2 is assigned to a region'
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final store3Tstds1 = storeFactoryService.store(3, tstds1)
      final store3Tstds2 = storeFactoryService.store(3, tstds2)
      final region2Tstds2 = regions[1]
      // this make the test failed (no store return) if there are no company_id column in region_to_store
      storeFactoryService.companyStoresToRegion(region2Tstds2, store3Tstds2)

      when:
      def result = get("$path/$store3Tstds1.id")

      then: 'store should not have a assigned region'
      notThrown(HttpClientResponseException)
      result.id == store3Tstds1.id
      result.storeNumber == store3Tstds1.number
      result.name == store3Tstds1.name
      result.region == null
   }

   void "fetch one store by id not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      with(response) {
         message == "0 was unable to be found"
         code == "system.not.found"
      }
   }

   void "fetch one store from different dataset than one associated with authenticated user" () {
      given:
      def company = companyFactoryService.forDatasetCode('tstds2')
      def store = storeFactoryService.store(4, company)

      when:
      get("$path/${store.myId()}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${store.myId()} was unable to be found"
   }

   void "fetch all stores as user who can see all stores" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 3
      pageOneResult.elements[0].id == 1
      pageOneResult.elements[0].storeNumber == 1
      pageOneResult.elements[0].name == "KANSAS CITY"
      pageOneResult.elements[0].region.name == regions[0].name
      pageOneResult.elements[0].region.division.name == divisions[0].name
      pageOneResult.elements[1].id == 2
      pageOneResult.elements[1].storeNumber == 3
      pageOneResult.elements[1].name == "INDEPENDENCE"
      pageOneResult.elements[1].region == null

      when:
      get("${path}${pageTwo}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "fetch all stores as a user who can only see the store they are assigned" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final department = departmentFactoryService.department('SA', company)
      final store = storeFactoryService.store(1, company)
      final singleStoreUser = employeeFactoryService.singleAuthenticated(company, store, department, 'N', 1)
      final singleStoreUserToken = loginEmployee(singleStoreUser)
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")

      when:
      def pageOneResult = get("${path}${pageOne}", singleStoreUserToken)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.elements != null
      pageOneResult.elements.size() == 1
      pageOneResult.elements[0].id == 1
      pageOneResult.elements[0].storeNumber == 1
      pageOneResult.elements[0].name == "KANSAS CITY"
      pageOneResult.elements[0].region.name == regions[0].name
      pageOneResult.elements[0].region.division.name == divisions[0].name
      pageOneResult.first == true
      pageOneResult.last == true
   }

   void "fetch all stores for a regional manager" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final department = departmentFactoryService.department('RM', company)
      final store = storeFactoryService.store(1, company)
      final singleStoreUser = employeeFactoryService.singleAuthenticated(company, store, department, 'R', regions[0].number)
      final singleStoreUserToken = loginEmployee(singleStoreUser)
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")

      when:
      def pageOneResult = get("${path}${pageOne}", singleStoreUserToken)

      then: 'Only store 1 assigned to a region'
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 1
      pageOneResult.elements[0].id == 1
      pageOneResult.elements[0].storeNumber == 1
      pageOneResult.elements[0].name == "KANSAS CITY"
      pageOneResult.elements[0].region.name == regions[0].name
      pageOneResult.elements[0].region.division.name == divisions[0].name
      pageOneResult.first == true
      pageOneResult.last == true
   }

   void "fetch all stores for a divisional manager" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final department = departmentFactoryService.department('EX', company)
      final store = storeFactoryService.store(1, company)
      final singleStoreUser = employeeFactoryService.singleAuthenticated(company, store, department, 'D', divisions[0].number)
      final singleStoreUserToken = loginEmployee(singleStoreUser)
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")

      when:
      def pageOneResult = get("${path}${pageOne}", singleStoreUserToken)

      then: 'Only store 1 assigned to a region'
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 1
      pageOneResult.elements[0].id == 1
      pageOneResult.elements[0].storeNumber == 1
      pageOneResult.elements[0].name == "KANSAS CITY"
      pageOneResult.elements[0].region.name == regions[0].name
      pageOneResult.elements[0].region.division.name == divisions[0].name
      pageOneResult.first == true
      pageOneResult.last == true
   }
}
