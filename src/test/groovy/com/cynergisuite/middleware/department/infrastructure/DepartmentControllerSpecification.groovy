package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.department.DepartmentValueObject
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject
import org.apache.commons.lang3.StringUtils


import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static org.apache.commons.lang3.StringUtils.trimToNull

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
      result.defaultMenu == trimToNull(department.defaultMenu)
   }

   void "fetch one by department id that doesn't exist"() {
      when:
      get("/department/110")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NOT_FOUND
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "110 was unable to be found"
   }

   void "fetch all departments" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      final pageThree = new StandardPageRequest(3, 5, "id", "ASC")
      final allTestDepartments = DepartmentFactory.all().collect { new DepartmentValueObject(it) }

      when:
      def pageOneResult = get("/department${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 9
      pageOneResult.elements.collect { new DepartmentValueObject(it) } == allTestDepartments[0..4]

      when:
      def pageTwoResult = get("/department${pageTwo}")

      then:
      notThrown(HttpClientResponseException)
      new StandardPageRequest(pageTwoResult.requested) == pageTwo
      pageTwoResult.elements != null
      pageTwoResult.elements.size() == 4
      pageTwoResult.totalElements == 9
      pageTwoResult.elements.collect { new DepartmentValueObject(it) } == allTestDepartments[5..8]

      when:
      get("/department${pageThree}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NO_CONTENT
   }
}
