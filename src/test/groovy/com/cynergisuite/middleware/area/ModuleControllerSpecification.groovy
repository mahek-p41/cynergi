package com.cynergisuite.middleware.area


import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class ModuleControllerSpecification extends ControllerSpecificationBase {

   void "create a module level" () {
      given:
      def module = moduleDataLoaderService.singleDTO(3, 9)

      when:
      def response = post("/module/3", module)

      then:
      notThrown(HttpClientResponseException)
      response.id == module.id
      response.level == module.level

      when:
      def response2 = get("/area")

      then:
      notThrown(HttpClientResponseException)
      def moduleWithId4 = response2[0].menus[1].modules[2]
      moduleWithId4.id == module.id
      moduleWithId4.level == module.level
   }

   void "create a module level that already exist" () {
      given:
      def module = moduleDataLoaderService.singleDTO(1, 9)

      when:
      post("/module/1", module)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == 'Config for module 1 already exists'
      response[0].code == 'cynergi.validation.config.exists'
      response[0].path == 'id'
   }

   void "create level for a non-existing module" () {
      given:
      def module = moduleDataLoaderService.singleDTO(999, 9)

      when:
      post("/module/999", module)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == '999 was unable to be found'
      response[0].path == 'id'
   }

   void "update a module level" () {
      given:
      def module = moduleDataLoaderService.singleDTO(1, 9)

      when:
      def response = put("/module/1", module)

      then:
      notThrown(HttpClientResponseException)
      response.id == module.id
      response.level == module.level

      when:
      def response2 = get("/area")

      then:
      notThrown(HttpClientResponseException)
      def moduleWithId1 = response2[0].menus[1].modules[0]
      moduleWithId1.id == module.id
      moduleWithId1.level == module.level
   }

   void "update a module level for non-existing module" () {
      given:
      def module = moduleDataLoaderService.singleDTO(999, 9)

      when:
      put("/module/999", module)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response[0].message == '999 was unable to be found'
      response[0].path == 'id'
   }

   void "update a module level for non-existing level config" () {
      given:
      def module = moduleDataLoaderService.singleDTO(4, 9)

      when:
      put("/module/4", module)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == 'Config for module 4 was unable to be found'
      response[0].path == 'id'
   }

}
