package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.employee.EmployeePageRequest
import com.google.common.net.UrlEscapers
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class EmployeeControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/employee'

   void "fetch all" () {
      given:
      def pageOne = new EmployeePageRequest([page: 1, size:  20, sortBy:  'id', sortDirection: 'ASC'])
      def pageTwo = new EmployeePageRequest([page: 2, size:  20, sortBy:  'id', sortDirection: 'ASC'])
      def pageLast = new EmployeePageRequest([page: 3, size:  20, sortBy:  'id', sortDirection: 'ASC'])
      def pageNine = new EmployeePageRequest([page: 9, size:  20, sortBy:  'id', sortDirection: 'ASC'])

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new EmployeePageRequest(it) } == pageOne
      pageOneResult.totalElements == 28
      pageOneResult.totalPages == 2
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 20
      pageOneResult.elements.collect().equals(pageOneResult.elements.collect().sort { it.id })

      when:
      get("$path/${pageNine}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "filter employee" () {
      given:
      def filterOne = new EmployeePageRequest([page: 1, size:  10, sortBy:  'id', sortDirection: 'ASC', lastName: 'HOME%20OFFICE'])
      def filterTwo = new EmployeePageRequest([lastName: 'HOME%20OFFICE'])
      def filterThree = new EmployeePageRequest([page: 1, size:  10, sortBy:  'id', sortDirection: 'ASC', firstNameMi: 'MARK'])

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
         number == 90000
         lastName == 'HOME OFFICE'
         alternativeStoreIndicator == 'A'
         alternativeArea == 0
         type == 'sysz'
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
         number == 90000
         lastName == 'HOME OFFICE'
         alternativeStoreIndicator == 'A'
         alternativeArea == 0
         type == 'sysz'
      }

      when:
      def filterThreeResult = get("$path${filterThree}")

      then:
      filterThreeResult.requested.with { new EmployeePageRequest(it) } == filterThree
      filterThreeResult.totalElements == 2
      filterThreeResult.totalPages == 1
      filterThreeResult.first == true
      filterThreeResult.last == true
      filterThreeResult.elements.size() == 2
      with(filterThreeResult.elements[0]) {
         number == 4013
         lastName == 'LEWIS'
         firstNameMi == 'MARK'
         alternativeStoreIndicator == 'A'
         alternativeArea == 0
         type == 'sysz'
         passCode == null
         store == null
         active == null
      }
   }

   @Unroll
   void "search employee #searchKey"() {
      when: 'search for STORE MANAGER with paging request params'
      def searchOneResult = get("$path${searchQuery}")

      then:
      with(searchOneResult.elements[0]) {
         number == firstElementUserNumber
         lastName == firstElementUserName
         firstNameMi == firstElementUserFirstName
         alternativeStoreIndicator == 'A'
         alternativeArea == 0
         type == 'sysz'
         passCode == null
         store == null
         active == null
      }

      where:
      searchQuery                                                                                                       | searchKey       || firstElementUserNumber | firstElementUserName   | firstElementUserFirstName
      new EmployeePageRequest([page: 1, size:  10, search: UrlEscapers.urlFragmentEscaper().escape('CRASH TEST')])     | 'Crash Test'   || 200                      | 'TEST'                 | 'CRASH'
      new EmployeePageRequest([search: UrlEscapers.urlFragmentEscaper().escape('cra test')])                           | 'cra test'     || 200                      | 'TEST'                 | 'CRASH'
      new EmployeePageRequest([search: UrlEscapers.urlFragmentEscaper().escape('tes test')])                           | 'tes test'     || 313331                   | 'TEST'                 | 'TEST'
      new EmployeePageRequest([search: UrlEscapers.urlFragmentEscaper().escape('st test')])                            | 'St Test'      || 313331                   | 'TEST'                 | 'TEST'
   }
}
