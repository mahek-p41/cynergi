package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.employee.EmployeePageRequest
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class EmployeeControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/employee'

   void "fetch all" () {
      given:
      def pageOne = new EmployeePageRequest([page: 1, size:  20, sortBy:  'id', sortDirection: 'ASC'])
      def pageTwo = new EmployeePageRequest([page: 2, size:  20, sortBy:  'id', sortDirection: 'ASC'])
      def pageLast = new EmployeePageRequest([page: 3, size:  20, sortBy:  'id', sortDirection: 'ASC'])
      def pageFour = new EmployeePageRequest([page: 4, size:  20, sortBy:  'id', sortDirection: 'ASC'])

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new EmployeePageRequest(it) } == pageOne
      pageOneResult.totalElements == 48
      pageOneResult.totalPages == 3
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 20
      with(pageOneResult.elements[0]) {
         number == 1
         lastName == 'ROUTE 1'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when:
      def pageTwoResult = get("$path${pageTwo}")

      then:
      pageTwoResult.requested.with { new EmployeePageRequest(it) } == pageTwo
      pageTwoResult.totalElements == 48
      pageTwoResult.totalPages == 3
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 20
      with(pageTwoResult.elements[0]) {
         number == 302
         lastName == 'ADAME'
         firstNameMi == 'JOSEPH'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when:
      def pageThreeResult = get("$path${pageLast}")

      then:
      pageThreeResult.requested.with { new EmployeePageRequest(it) } == pageLast
      pageThreeResult.totalElements == 48
      pageThreeResult.totalPages == 3
      pageThreeResult.first == false
      pageThreeResult.last == true
      pageThreeResult.elements.size() == 8
      with(pageThreeResult.elements[0]) {
         number == 90003
         lastName == 'STORE MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when:
      get("$path/${pageFour}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "filter employee" () {
      given:
      def filterOne = new EmployeePageRequest([page: 1, size:  10, sortBy:  'id', sortDirection: 'ASC', lastName: 'STORE%20MANAGER'])
      def filterTwo = new EmployeePageRequest([lastName: 'STORE%20MANAGER'])
      def filterThree = new EmployeePageRequest([page: 1, size:  10, sortBy:  'id', sortDirection: 'ASC', firstNameMi: 'EMP'])

      when:
      def filterOneResult = get("$path${filterOne}")

      then:
      filterOneResult.requested.with { new EmployeePageRequest(it) } == filterOne
      filterOneResult.totalElements == 1
      filterOneResult.totalPages == 1
      filterOneResult.first == true
      filterOneResult.last == true
      filterOneResult.elements.size() == 1
      with(filterOneResult.elements[0]) {
         number == 90003
         lastName == 'STORE MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when:
      def filterTwoResult = get("$path${filterTwo}")

      then:
      filterTwoResult.requested.with { new EmployeePageRequest(it) } == filterOne
      filterTwoResult.totalElements == 1
      filterTwoResult.totalPages == 1
      filterTwoResult.first == true
      filterTwoResult.last == true
      filterTwoResult.elements.size() == 1
      with(filterTwoResult.elements[0]) {
         number == 90003
         lastName == 'STORE MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when:
      def filterThreeResult = get("$path${filterThree}")

      then:
      filterThreeResult.requested.with { new EmployeePageRequest(it) } == filterThree
      filterThreeResult.totalElements == 1
      filterThreeResult.totalPages == 1
      filterThreeResult.first == true
      filterThreeResult.last == true
      filterThreeResult.elements.size() == 1
      with(filterThreeResult.elements[0]) {
         number == 90008
         lastName == 'TERMINATED'
         firstNameMi == 'EMP'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }
   }

   void "search employee" () {
      given:
      def searchOne = new EmployeePageRequest([page: 1, size:  10, search: 'Store%20Manager'])
      def searchTwo = new EmployeePageRequest([search: 'store%20manager'])
      def searchThree = new EmployeePageRequest([search: 'Emp'])
      def searchFour = new EmployeePageRequest([search: 'Karager'])
      def searchFive = new EmployeePageRequest([search: 'St%20Manag'])

      when: 'search for STORE MANAGER with paging request params'
      def searchOneResult = get("$path${searchOne}")

      then:
      searchOneResult.totalElements == 7
      searchOneResult.totalPages == 1
      searchOneResult.first == true
      searchOneResult.last == true
      searchOneResult.elements.size() == 7

      with(searchOneResult.elements[0]) {
         number == 90003
         lastName == 'STORE MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      with(searchOneResult.elements[1]) {
         number == 90004
         lastName == 'ASST MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      with(searchOneResult.elements[2]) {
         number == 90002
         lastName == 'MARKET MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when: 'search for STORE MANAGER'
      def searchTwoResult = get("$path${searchTwo}")

      then:
      searchTwoResult.totalElements == 7
      searchTwoResult.totalPages == 1
      searchTwoResult.first == true
      searchTwoResult.last == true
      searchTwoResult.elements.size() == 7

      with(searchTwoResult.elements[0]) {
         number == 90003
         lastName == 'STORE MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      with(searchTwoResult.elements[1]) {
         number == 90004
         lastName == 'ASST MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      with(searchTwoResult.elements[2]) {
         number == 90002
         lastName == 'MARKET MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when: 'search for EMP'
      def searchThreeResult = get("$path${searchThree}")

      then:
      searchThreeResult.totalElements == 1
      searchThreeResult.totalPages == 1
      searchThreeResult.first == true
      searchThreeResult.last == true
      searchThreeResult.elements.size() == 1

      with(searchThreeResult.elements[0]) {
         number == 90008
         lastName == 'TERMINATED'
         firstNameMi == 'EMP'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when: 'search for Karager'
      def searchFourResult = get("$path${searchFour}")

      then:
      searchFourResult.totalElements == 3
      searchFourResult.totalPages == 1
      searchFourResult.first == true
      searchFourResult.last == true
      searchFourResult.elements.size() == 3

      with(searchFourResult.elements[0]) {
         number == 90002
         lastName == 'MARKET MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      with(searchFourResult.elements[1]) {
         number == 90004
         lastName == 'ASST MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      with(searchFourResult.elements[2]) {
         number == 90003
         lastName == 'STORE MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      when: 'search for St Man'
      def searchFiveResult = get("$path${searchFive}")

      then:
      searchFiveResult.totalElements == 5
      searchFiveResult.totalPages == 1
      searchFiveResult.first == true
      searchFiveResult.last == true
      searchFiveResult.elements.size() == 5

      with(searchFiveResult.elements[0]) {
         number == 90003
         lastName == 'STORE MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      with(searchFiveResult.elements[1]) {
         number == 90004
         lastName == 'ASST MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }

      with(searchFiveResult.elements[2]) {
         number == 90002
         lastName == 'MARKET MANAGER'
         alternativeStoreIndicator == 'N'
         alternativeArea == 0
      }
   }
}
