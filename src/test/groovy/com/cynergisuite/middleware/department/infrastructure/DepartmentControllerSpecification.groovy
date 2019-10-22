package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.department.DepartmentFactoryService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

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
}
