package com.cynergisuite.middleware.area

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class AreaControllerSpecification extends ControllerSpecificationBase {
   @Inject MenuDataLoaderService menuDataLoaderService

   void "fetch all areas" () {
      given:
      def predefinedTstds1Areas = areaDataLoaderService.predefined().findAll { it.company.myDataset() == "tstds1" }.collect { new AreaDTO(it) }
      def area1Menus = menuDataLoaderService.predefined().findAll { it.areaType.id == 1 }.collect { new MenuTypeDTO(it) }
      def menu1Modules = moduleDataLoaderService.predefined().findAll { it.menuType.id == 1 }.collect { new ModuleTypeDTO(it) }
      def menu2Modules = moduleDataLoaderService.predefined().findAll { it.menuType.id == 5 }.collect { new ModuleTypeDTO(it) }

      when:
      def response = get( "/area")

      then:
      notThrown(HttpClientResponseException)
      response.size() == 5
      with(response[0]) {
         def predefinedArea = predefinedTstds1Areas[0]
         it.id == predefinedArea.areaType.id
         it.value == predefinedArea.areaType.value
         it.description == predefinedArea.areaType.description
         it.enabled == true

         it.menus.size() == area1Menus.size()

         with(it.menus[0]) {
            it.id == area1Menus[0].id
            it.value == area1Menus[0].value
            it.description == area1Menus[0].description

            it.modules.size() == menu1Modules.size()
            with(it.modules[0]) {
               it.id == menu1Modules[0].id
               it.value == menu1Modules[0].value
               it.description == menu1Modules[0].description
               it.level == 15
            }
            with(it.modules[1]) {
               it.id == menu1Modules[1].id
               it.value == menu1Modules[1].value
               it.description == menu1Modules[1].description
               it.level == 100
            }
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
}
