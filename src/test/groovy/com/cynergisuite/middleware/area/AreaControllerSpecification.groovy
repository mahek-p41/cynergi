package com.cynergisuite.middleware.area

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AreaControllerSpecification extends ControllerSpecificationBase {
   @Inject MenuDataLoaderService menuDataLoaderService

   void "fetch all areas" () {
      given:
      def predefinedAreas = AreaDataLoader.areaTypes()
      def area1Menus = menuDataLoaderService.predefined().findAll { it.areaType.id == 1 }.collect { new MenuDTO(it) }
      def menu2Modules = moduleDataLoaderService.predefined().findAll { it.menuType.id == 2 }.collect { new ModuleDTO(it) }

      when:
      def response = get("/area")

      then:
      notThrown(HttpClientResponseException)
      response.size() == 6
      with(response[0]) {
         def predefinedArea = predefinedAreas[0]
         id == predefinedArea.id
         value == predefinedArea.value
         description == predefinedArea.description
         enabled == true

         menus.size() == area1Menus.size()

         with(menus[0]) {
            id == area1Menus[0].id
            value == area1Menus[0].value
            description == area1Menus[0].description

            modules.size() == 0
         }

         with(menus[1]) {
            id == area1Menus[1].id
            value == area1Menus[1].value
            description == area1Menus[1].description

            modules.size() == menu2Modules.size()
            with(modules[0]) {
               id == menu2Modules[0].id
               value == menu2Modules[0].value
               description == menu2Modules[0].description
               level == 10
            }
            with(modules[1]) {
               id == menu2Modules[1].id
               value == menu2Modules[1].value
               description == menu2Modules[1].description
               level == 15
            }
         }
      }

      and: 'assert nested menus'
      def purchaseOrderArea = response[3]
      def purchaseOrderMenus = menuDataLoaderService.predefined().findAll { it.areaType.id == 4 }.collect { new MenuDTO(it) }
      def poMaintenanceMenu = purchaseOrderMenus[0]
      def poReportMenu = purchaseOrderMenus[1]
      def stockReorderMenu = purchaseOrderMenus[3]

      with(purchaseOrderArea) {
         def predefinedArea = predefinedAreas[3]
         id == predefinedArea.id
         value == predefinedArea.value
         description == 'Purchase Order'
         enabled == true

         menus.size() == 2
         menus.sort { o1, o2 -> o1.id <=> o2.id}

         with(menus[0]) {
            id == poMaintenanceMenu.id
            value == poMaintenanceMenu.value
            description == poMaintenanceMenu.description

            menus.size() == 0
            modules.size() == 23
         }

         with(menus[1]) {
            id == poReportMenu.id
            value == poReportMenu.value
            description == poReportMenu.description

            menus.size() == 5
            modules.size() == 11

            with(menus[1]) {
               id == stockReorderMenu.id
               value == stockReorderMenu.value
               description == stockReorderMenu.description

               menus.size() == 0
               modules.size() == 1
            }
         }
      }
   }

   void "enable/disable an area" () {
      when:
      def response = get("/area")

      then: 'Area 3 is not enabled by default'
      notThrown(HttpClientResponseException)
      response.size() == 6
      response.find { it.id == 3 }.enabled == false

      when:
      post("/area", new SimpleLegacyIdentifiableDTO(3))

      then: 'Area 3 is enabled'
      notThrown(HttpClientResponseException)

      when:
      def response2 = get("/area")

      then:
      notThrown(HttpClientResponseException)
      response2.size() == 6
      response2.find { it.id == 3 }.enabled == true

      when:
      delete("/area/3")

      then: 'Area 3 is disabled'
      notThrown(HttpClientResponseException)

      when:
      def response3 = get("/area")

      then:
      notThrown(HttpClientResponseException)
      response3.size() == 5
      response3.find { it.id == 3 }.enabled == false
   }

   void "enable/disable an invalid area" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      post("/area", new SimpleIdentifiableDTO(nonExistentId))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.message == "Failed to convert argument [id] for value [$nonExistentId]"
      response.path == "id"

      when:
      delete("/area/99")

      then:
      final exception2 = thrown(HttpClientResponseException)
      exception2.response.status == NOT_FOUND
      def response2 = exception.response.bodyAsJson()
      response2.message == "Failed to convert argument [id] for value [$nonExistentId]"
      response2.path == "id"
   }
}
