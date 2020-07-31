package com.cynergisuite.middleware.area

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class AreaControllerSpecification extends ControllerSpecificationBase {
   @Inject MenuDataLoaderService menuDataLoaderService

   void "fetch all areas" () {
      given:
      def predefinedAreas = AreaDataLoader.areaTypes()
      def area1Menus = menuDataLoaderService.predefined().findAll { it.areaType.id == 1 }.collect { new MenuDTO(it) }
      def menu2Modules = moduleDataLoaderService.predefined().findAll { it.menuType.id == 5 }.collect { new ModuleDTO(it) }

      when:
      def response = get( "/area")

      then:
      notThrown(HttpClientResponseException)
      response.size() == 5
      with(response[0]) {
         def predefinedArea = predefinedAreas[0]
         it.id == predefinedArea.id
         it.value == predefinedArea.value
         it.description == predefinedArea.description
         it.enabled == true

         it.menus.size() == area1Menus.size()

         with(it.menus[0]) {
            it.id == area1Menus[0].id
            it.value == area1Menus[0].value
            it.description == area1Menus[0].description

            it.modules.size() == 0
         }

         with(it.menus[1]) {
            it.id == area1Menus[1].id
            it.value == area1Menus[1].value
            it.description == area1Menus[1].description

            it.modules.size() == menu2Modules.size()
            with(it.modules[0]) {
               it.id == menu2Modules[0].id
               it.value == menu2Modules[0].value
               it.description == menu2Modules[0].description
               it.level == 10
            }
            with(it.modules[1]) {
               it.id == menu2Modules[1].id
               it.value == menu2Modules[1].value
               it.description == menu2Modules[1].description
               it.level == 100
            }
         }
      }
   }

   void "enable/disable an area" () {
      when:
      def response = get( "/area")

      then: 'Area 3 is not enabled by default'
      notThrown(HttpClientResponseException)
      response.size() == 5
      response.find { it.id == 3 }.enabled == false

      when:
      post( "/area", new SimpleIdentifiableDTO(3))

      then: 'Area 3 is enabled'
      notThrown(HttpClientResponseException)

      when:
      def response2 = get( "/area")

      then:
      notThrown(HttpClientResponseException)
      response2.size() == 5
      response2.find { it.id == 3 }.enabled == true

      when:
      delete( "/area", new SimpleIdentifiableDTO(3))

      then: 'Area 3 is disabled'
      notThrown(HttpClientResponseException)

      when:
      def response3 = get( "/area")

      then:
      notThrown(HttpClientResponseException)
      response3.size() == 5
      response3.find { it.id == 3 }.enabled == false
   }

   void "enable/disable an invalid area" () {
      when:
      post( "/area", new SimpleIdentifiableDTO(99))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == '99 was unable to be found'
      response[0].path == 'areaTypeId'

      when:
      delete( "/area", new SimpleIdentifiableDTO(99))

      then:
      final exception2 = thrown(HttpClientResponseException)
      exception2.response.status == BAD_REQUEST
      def response2 = exception.response.bodyAsJson()
      response2.size() == 1
      response2[0].message == '99 was unable to be found'
      response2[0].path == 'areaTypeId'
   }

}
