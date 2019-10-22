package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.department.DepartmentValueObject
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class DepartmentControllerSpecification extends ControllerSpecificationBase {
   @Inject DepartmentFactoryService departmentFactoryService

   void "fetch one by department id" () {
      given:
      def department = departmentFactoryService.random()

      when:
      def result = get("/department/${department.id}")

      then:
      notThrown(HttpClientResponseException)
      result.id == department.id
      result.code == department.code
      result.description == department.description
      result.securityProfile == department.securityProfile
      result.defaultMenu == department.defaultMenu
   }

   void "fetch one by department id that doesn't exist"() {
      when:
      get("/department/11")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      final response = exception.response.body().with { parseResponse(it) }
      response.size() == 1
      response.message == "11 was unable to be found"
   }

   void "fetch all departments" () {
      given:
      final pageOne = new PageRequest(1, 5, "id", "ASC")
      final pageTwo = new PageRequest(2, 5, "id", "ASC")
      final pageThree = new PageRequest(3, 5, "id", "ASC")
      final allTestDepartments = DepartmentFactory.all().collect { new DepartmentValueObject(it) }

      when:
      def pageOneResult = get("/department${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == allTestDepartments.size()
      pageOneResult.elements.collect { new DepartmentValueObject(it) } == allTestDepartments[0..4]

      when:
      def pageTwoResult = get("/department${pageTwo}")

      then:
      notThrown(HttpClientResponseException)
      new PageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 5
      pageTwoResult.totalElements == allTestDepartments.size()
      pageTwoResult.elements.collect { new DepartmentValueObject(it) } == allTestDepartments[5..9]

      when:
      get("/department${pageThree}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      final def notFoundResult = exception.response.bodyAsJson()
      notFoundResult.size() == 1
      notFoundResult.message == "Request with Page 3, Size 5, Sort By id and Sort Direction ASC produced no results"
   }
}
