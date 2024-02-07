package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class StoreControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/store"

   void "fetch one store by id with the a region assigned" () {
      given: 'store number 1 is assigned to a region of company tstds1'
      final company = companyFactoryService.forDatasetCode('coravt')
      final store1 = storeFactoryService.store(3, company)

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
      given: 'store store1Tstds1 is not assigned to any region, store store10Tstds2 is assigned to a region'
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      final store1Tstds1 = storeFactoryService.store(1, tstds1)
      final store10Tstds2 = storeFactoryService.store(10, tstds2)
      final region2Tstds2 = regions[1]
      // this make the test failed (no store return) if there are no company_id column in region_to_store
      storeFactoryService.companyStoresToRegion(region2Tstds2, store10Tstds2)

      when:
      def result = get("$path/$store1Tstds1.id")

      then: 'store should not have a assigned region'
      notThrown(HttpClientResponseException)
      result.id == store1Tstds1.id
      result.storeNumber == store1Tstds1.number
      result.name == store1Tstds1.name
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
         code == 'system.not.found'
      }
   }

   void "fetch one store from different dataset than one associated with authenticated user" () {
      given:
      def company = companyFactoryService.forDatasetCode('corrto')
      def store = storeFactoryService.store(106, company)

      when:
      get("$path/${store.myId()}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${store.myId()} was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all stores as user who can see all stores" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")

      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.totalElements == 7
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.elements[0].id == 1
      pageOneResult.elements[0].storeNumber == 0
      pageOneResult.elements[0].name == "ADVANCED VENTURES,LLC"
      pageOneResult.elements[0].region == null
      pageOneResult.elements[3].id == 4
      pageOneResult.elements[3].storeNumber == 3
      pageOneResult.elements[3].name == "HATTIESBURG"
      pageOneResult.elements[3].region.name == regions[0].name
      pageOneResult.elements[3].region.division.name == divisions[0].name
   }

   void "fetch all stores as a user who can only see the store they are assigned" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final department = departmentFactoryService.department('SL', company)
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
      pageOneResult.elements[0].id == 2
      pageOneResult.elements[0].storeNumber == 1
      pageOneResult.elements[0].name == "HOUMA"
      pageOneResult.elements[0].region == null
      pageOneResult.first == true
      pageOneResult.last == true
   }

   void "fetch all stores for a regional manager" () {
      given:
      final company = companyFactoryService.forDatasetCode('corrto')
      final department = departmentFactoryService.department('RM', company)
      final store = storeFactoryService.store(1, company)
      final singleStoreUser = employeeFactoryService.singleAuthenticated(company, store, department, 'N', regions[0].number)
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
      pageOneResult.elements[0].name == "Derby"
      pageOneResult.elements[0].region == null
      pageOneResult.first == true
      pageOneResult.last == true
   }

   void "fetch all stores for a divisional manager" () {
      given:
      final company = companyFactoryService.forDatasetCode('corrto')
      final department = departmentFactoryService.department('EX', company)
      final store = storeFactoryService.store(1, company)
      final singleStoreUser = employeeFactoryService.singleAuthenticated(company, store, department, 'N', divisions[0].number)
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
      pageOneResult.elements[0].name == "Derby"
      pageOneResult.elements[0].region == null
      pageOneResult.first == true
      pageOneResult.last == true
   }

   void "search store by store number and name" () {
      given:
      final pageOne = new SearchPageRequest(1, 5, "1", true)
      final pageTwo = new SearchPageRequest(1, 5, "HATTIESBURG", true)

      when:
      def pageOneResult = get("${path}/search${pageOne}")

      then: 'Only store 1 is returned'
      notThrown(HttpClientResponseException)
      new SearchPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 1
      pageOneResult.elements[0].id == 2
      pageOneResult.elements[0].storeNumber == 1
      pageOneResult.elements[0].name == "HOUMA"
      pageOneResult.elements[0].region == null
      pageOneResult.first == true
      pageOneResult.last == true

      when:
      def pageTwoResult = get("${path}/search${pageTwo}")

      then: 'Only store HATTIESBURG is returned'
      notThrown(HttpClientResponseException)
      new SearchPageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 1
      pageTwoResult.elements[0].id == 4
      pageTwoResult.elements[0].storeNumber == 3
      pageTwoResult.elements[0].name == "HATTIESBURG"
      pageOneResult.elements[0].region == null
      pageTwoResult.first == true
      pageTwoResult.last == true
   }

   void "search stores with fuzzy searching" () {
      given:
      def query = ""
      switch (criteria) {
         case ' 1':
            query = "%201"
            break
         case '90 ':
            query = "90%20"
            break
         case 'ADVANCED':
            query = "ADVANCED"
            break
      }

      when:
      def result = get("$path/search?query=$query")

      then:
      notThrown(HttpClientException)
      result != null
      result.elements.size() == searchResultsCount
      where:
      criteria       || searchResultsCount
      ' 1'           || 1
      '90 '          || 1
      'ADVANCED'     || 2
   }

   void "search stores with fuzzy searching no results found" () {
      given:
      def query = ""
      switch (criteria) {
         case 'CITY':
            query = "CITY"
            break
         case '23':
            query = "23"
            break
      }

      when:
      get("$path/search?fuzzy=false&query=$query")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == response
      where:
      criteria       || response
      'CITY'         || NO_CONTENT
      '23'           || NO_CONTENT
   }

   void "search stores with strict searching" () {
      given:
      def query = ""
      switch (criteria) {
         case '   3  ':
            query = "%20%20%203%20%20"
            break
         case 'ADVANCED':
            query = "ADVANCED"
            break
      }

      when:
      def result = get("$path/search?fuzzy=false&query=$query")

      then:
      notThrown(HttpClientException)
      result != null
      result.elements.size() == searchResultsCount
      where:
      criteria       || searchResultsCount
      '   3  '       || 1
      'ADVANCED'     || 2
   }

   void "search stores with strict searching no results found" () {
      given:
      def query = ""
      switch (criteria) {
         case 'CITY':
            query = "CITY"
            break
         case '90':
            query = "90"
            break
      }

      when:
      get("$path/search?fuzzy=false&query=$query")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == response
      where:
      criteria       || response
      'CITY'         || NO_CONTENT
      '90'           || NO_CONTENT
   }

   void "search stores by both number and name" () {
      given:
      def query = ""
      switch (criteria) {
         case '1 - HOUMA':
            query = "1%20-%20HOUMA"
            break
         case '9000 - ADVANCED VENTURES,LLC':
            query = "9000%20-%20ADVANCED%20VENTURES,LLC"
            break
      }

      when:
      def result = get("$path/search?query=$query")

      then:
      notThrown(HttpClientException)
      result != null
      result.elements.size() == searchResultsCount

      where:
      criteria                         || searchResultsCount
      '1 - HOUMA'                      || 1
      '9000 - ADVANCED VENTURES,LLC'   || 1
   }

   void "search stores by both number and name no results found" () {
      given:
      def query = ""
      switch (criteria) {
         case '1 -HOUMA':
            query = "1%20-HOUMA"
            break
         case '1 - HATTIESBURG':
            query = "1%20-%20HATTIESBURG"
            break
         case '900 - ADVANCED VENTURES,LLC':
            query = "900%20-%20ADVANCED%20VENTURES,LLC"
            break
      }

      when:
      get("$path/search?query=$query")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == response
      where:
      criteria                || response
      '1 -HOUMA'             || NO_CONTENT
      '1 - HATTIESBURG'      || NO_CONTENT
      '900 - ADVANCED VENTURES,LLC'     || NO_CONTENT
   }

   void "search stores sql injection" () {
      when: "Throw SQL Injection at it"
      get("$path/search?query=%20or%201=1;drop%20table%20system_stores_fimvw;--")

      then:
      def ex = thrown(HttpClientResponseException)
      ex.response.status == NO_CONTENT
   }
}
