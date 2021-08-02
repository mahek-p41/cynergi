package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.department.DepartmentDTO
import com.cynergisuite.middleware.department.DepartmentFactory
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class DepartmentControllerSpecification extends ControllerSpecificationBase {

   void "fetch one by department id" () {
      given:
      def department = departmentFactoryService.random(nineNineEightAuthenticatedEmployee.company)

      when:
      def result = get("/department/${department.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == department.id
      result.code == department.code
      result.description == department.description
   }

   void "fetch one by department not associated with authenticated user's dataset" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds2')
      final department = departmentFactoryService.random(company)

      when:
      get("/department/${department.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      final response = exception.response.bodyAsJson()
      response.message == "${department.id} was unable to be found"
   }

   void "fetch one by department id that doesn't exist"() {
      when:
      get("/department/110")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      final response = exception.response.bodyAsJson()
      with(response) {
         message == "110 was unable to be found"
         code == "system.not.found"
      }
   }

   void "fetch all departments" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final pageThree = new StandardPageRequest(3, 5, "id", "ASC")
      final pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      final allTestDepartments = DepartmentFactory.all().collect { new DepartmentDTO(it) }

      when:
      def pageOneResult = get("/department${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 11
      pageOneResult.elements[0].id == allTestDepartments[0].id
      pageOneResult.elements[0].code == allTestDepartments[0].code
      pageOneResult.elements[0].description == allTestDepartments[0].description
      pageOneResult.elements[1].id == allTestDepartments[1].id
      pageOneResult.elements[1].code == allTestDepartments[1].code
      pageOneResult.elements[1].description == allTestDepartments[1].description
      pageOneResult.elements[2].id == allTestDepartments[2].id
      pageOneResult.elements[2].code == allTestDepartments[2].code
      pageOneResult.elements[2].description == allTestDepartments[2].description
      pageOneResult.elements[3].id == allTestDepartments[3].id
      pageOneResult.elements[3].code == allTestDepartments[3].code
      pageOneResult.elements[3].description == allTestDepartments[3].description
      pageOneResult.elements[4].id == allTestDepartments[4].id
      pageOneResult.elements[4].code == allTestDepartments[4].code
      pageOneResult.elements[4].description == allTestDepartments[4].description

      when:
      def pageTwoResult = get("/department${pageTwo}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.totalElements == 11
      pageTwoResult.elements[0].id == allTestDepartments[5].id
      pageTwoResult.elements[0].code == allTestDepartments[5].code
      pageTwoResult.elements[0].description == allTestDepartments[5].description
      pageTwoResult.elements[1].id == allTestDepartments[6].id
      pageTwoResult.elements[1].code == allTestDepartments[6].code
      pageTwoResult.elements[1].description == allTestDepartments[6].description
      pageTwoResult.elements[2].id == allTestDepartments[7].id
      pageTwoResult.elements[2].code == allTestDepartments[7].code
      pageTwoResult.elements[2].description == allTestDepartments[7].description
      pageTwoResult.elements[3].id == allTestDepartments[8].id
      pageTwoResult.elements[3].code == allTestDepartments[8].code
      pageTwoResult.elements[3].description == allTestDepartments[8].description
      pageTwoResult.elements[4].id == allTestDepartments[9].id
      pageTwoResult.elements[4].code == allTestDepartments[9].code
      pageTwoResult.elements[4].description == null

      when:
      def pageThreeResult = get("/department${pageThree}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageThreeResult.requested) == pageThree
      pageThreeResult.elements != null
      pageThreeResult.elements.size() == 1
      pageThreeResult.totalElements == 11
      pageThreeResult.elements[0].id == allTestDepartments[10].id
      pageThreeResult.elements[0].code == allTestDepartments[10].code

      when:
      get("/department${pageFour}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NO_CONTENT
   }
}
