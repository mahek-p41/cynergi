package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

@MicronautTest(transactional = false)
class InventoryControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/inventory"

   void "fetch first page" () {
      given:
      final pageOne = new InventoryPageRequest(new PageRequest(1, 5, "id", "ASC"), authenticatedEmployee.store.number)
      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == authenticatedEmployee.store.number
      pageOneResult.requested.inventoryStatus == null
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 400
      pageOneResult.totalPages == 80
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 2
      pageOneResult.elements[0].serialNumber == 'XB012730DI'
      pageOneResult.elements[0].barcodeNumber == 'XB012730DI'
      pageOneResult.elements[0].location == 1
      pageOneResult.elements[0].status == 'R'
      pageOneResult.elements[0].makeModelNumber == 'VCTOSP100'
      pageOneResult.elements[0].modelCategory == 'V'
      pageOneResult.elements[0].productCode == 'VC'
      pageOneResult.elements[0].description == 'VIDEO CAMERA RECORDER'
   }

   void "fetch first page of inventory with status of N" () {
      given:
      final pageOne = new InventoryPageRequest(new PageRequest(1, 5, "id", "ASC"), authenticatedEmployee.store.number, ["N"])
      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == authenticatedEmployee.store.number
      pageOneResult.requested.inventoryStatus.size() == 1
      pageOneResult.requested.inventoryStatus[0] == "N"
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 272
      pageOneResult.totalPages == 55
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 5
      pageOneResult.elements[0].serialNumber == '201-01287'
      pageOneResult.elements[0].barcodeNumber == '201-01287'
      pageOneResult.elements[0].location == 1
      pageOneResult.elements[0].status == 'N'
      pageOneResult.elements[0].makeModelNumber == 'BODON700FH'
      pageOneResult.elements[0].modelCategory == 'B'
      pageOneResult.elements[0].productCode == 'BO'
      pageOneResult.elements[0].description == 'BEDROOM OTHER'
   }
}
