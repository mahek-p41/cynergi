package com.cynergisuite.middleware.inventory.infrastructure


import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest(transactional = false)
class InventoryControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/inventory"

   @Inject InventoryRepository inventoryRepository

   void "fetch first page without locationType" () {
      given:
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, inventoryStatus: ["N", "O", "R", "D"]])
      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.locationType == null
      pageOneResult.requested.storeNumber == authenticatedEmployee.store.number
      pageOneResult.requested.inventoryStatus == ["R", "D", "N", "O"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 423
      pageOneResult.totalPages == 85
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 73821
      pageOneResult.elements[0].serialNumber == "201-00923"
      pageOneResult.elements[0].lookupKey == "201-00923"
      pageOneResult.elements[0].lookupKeyType == "BARCODE"
      pageOneResult.elements[0].barcode == "201-00923"
      pageOneResult.elements[0].altId == "201-00923"
      pageOneResult.elements[0].brand == "GIDGET MEAUT"
      pageOneResult.elements[0].modelNumber == "FPGIDFRAMEDART"
      pageOneResult.elements[0].productCode == "F-MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[0].description == "MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[0].receivedDate == "2012-04-13"
      pageOneResult.elements[0].originalCost == 55.03
      pageOneResult.elements[0].actualCost == 55.03
      pageOneResult.elements[0].modelCategory == "F"
      pageOneResult.elements[0].timesRented == 0
      pageOneResult.elements[0].totalRevenue == 0.00
      pageOneResult.elements[0].remainingValue == 0.00
      pageOneResult.elements[0].sellPrice == 0.00
      pageOneResult.elements[0].assignedValue == 0.00
      pageOneResult.elements[0].idleDays == 0
      pageOneResult.elements[0].condition == null
      pageOneResult.elements[0].status == "N"
      pageOneResult.elements[0].primaryLocation.id == 1
      pageOneResult.elements[0].primaryLocation.storeNumber == 1
      pageOneResult.elements[0].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[0].locationType.value == "STORE"
      pageOneResult.elements[0].locationType.description == "Store"
   }

   void "fetch first page with locationType" () {
      given:
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]])
      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == authenticatedEmployee.store.number
      pageOneResult.requested.locationType == "STORE"
      pageOneResult.requested.inventoryStatus == ["R", "D", "N", "O"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 423
      pageOneResult.totalPages == 85
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 73821
      pageOneResult.elements[0].serialNumber == "201-00923"
      pageOneResult.elements[0].lookupKey == "201-00923"
      pageOneResult.elements[0].lookupKeyType == "BARCODE"
      pageOneResult.elements[0].barcode == "201-00923"
      pageOneResult.elements[0].altId == "201-00923"
      pageOneResult.elements[0].brand == "GIDGET MEAUT"
      pageOneResult.elements[0].modelNumber == "FPGIDFRAMEDART"
      pageOneResult.elements[0].productCode == "F-MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[0].description == "MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[0].receivedDate == "2012-04-13"
      pageOneResult.elements[0].originalCost == 55.03
      pageOneResult.elements[0].actualCost == 55.03
      pageOneResult.elements[0].modelCategory == "F"
      pageOneResult.elements[0].timesRented == 0
      pageOneResult.elements[0].totalRevenue == 0.00
      pageOneResult.elements[0].remainingValue == 0.00
      pageOneResult.elements[0].sellPrice == 0.00
      pageOneResult.elements[0].assignedValue == 0.00
      pageOneResult.elements[0].idleDays == 0
      pageOneResult.elements[0].condition == null
      pageOneResult.elements[0].status == "N"
      pageOneResult.elements[0].primaryLocation.id == 1
      pageOneResult.elements[0].primaryLocation.storeNumber == 1
      pageOneResult.elements[0].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[0].locationType.value == "STORE"
      pageOneResult.elements[0].locationType.description == "Store"
   }

   void "fetch first page of inventory with status of N" () {
      given:
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, inventoryStatus: ["N"]])
      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == authenticatedEmployee.store.number
      pageOneResult.requested.inventoryStatus.size() == 1
      pageOneResult.requested.inventoryStatus == ["N"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 252
      pageOneResult.totalPages == 51
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 73821
      pageOneResult.elements[0].serialNumber == "201-00923"
      pageOneResult.elements[0].lookupKey == "201-00923"
      pageOneResult.elements[0].lookupKeyType == "BARCODE"
      pageOneResult.elements[0].barcode == "201-00923"
      pageOneResult.elements[0].altId == "201-00923"
      pageOneResult.elements[0].brand == "GIDGET MEAUT"
      pageOneResult.elements[0].modelNumber == "FPGIDFRAMEDART"
      pageOneResult.elements[0].productCode == "F-MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[0].description == "MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[0].receivedDate == "2012-04-13"
      pageOneResult.elements[0].originalCost == 55.03
      pageOneResult.elements[0].actualCost == 55.03
      pageOneResult.elements[0].modelCategory == "F"
      pageOneResult.elements[0].timesRented == 0
      pageOneResult.elements[0].totalRevenue == 0.00
      pageOneResult.elements[0].remainingValue == 0.00
      pageOneResult.elements[0].sellPrice == 0.00
      pageOneResult.elements[0].assignedValue == 0.00
      pageOneResult.elements[0].idleDays == 0
      pageOneResult.elements[0].condition == null
      pageOneResult.elements[0].status == "N"
      pageOneResult.elements[0].primaryLocation.id == 1
      pageOneResult.elements[0].primaryLocation.storeNumber == 1
      pageOneResult.elements[0].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[0].locationType.value == "STORE"
      pageOneResult.elements[0].locationType.description == "Store"
   }

   void "fetch by location type store"() {
      given:
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: authenticatedEmployee.store.number, locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]])

      when:
      def pageOneResult = get("${path}${pageOne}")

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == authenticatedEmployee.store.number
      pageOneResult.requested.inventoryStatus == ["R", "D", "N", "O"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 423
      pageOneResult.totalPages == 85
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 73821
      pageOneResult.elements[0].serialNumber == "201-00923"
      pageOneResult.elements[0].lookupKey == "201-00923"
      pageOneResult.elements[0].lookupKeyType == "BARCODE"
      pageOneResult.elements[0].barcode == "201-00923"
      pageOneResult.elements[0].altId == "201-00923"
      pageOneResult.elements[0].brand == "GIDGET MEAUT"
      pageOneResult.elements[0].modelNumber == "FPGIDFRAMEDART"
      pageOneResult.elements[0].productCode == "F-MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[0].description == "MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[0].receivedDate == "2012-04-13"
      pageOneResult.elements[0].originalCost == 55.03
      pageOneResult.elements[0].actualCost == 55.03
      pageOneResult.elements[0].modelCategory == "F"
      pageOneResult.elements[0].timesRented == 0
      pageOneResult.elements[0].totalRevenue == 0.00
      pageOneResult.elements[0].remainingValue == 0.00
      pageOneResult.elements[0].sellPrice == 0.00
      pageOneResult.elements[0].assignedValue == 0.00
      pageOneResult.elements[0].idleDays == 0
      pageOneResult.elements[0].condition == null
      pageOneResult.elements[0].status == "N"
      pageOneResult.elements[0].primaryLocation.id == 1
      pageOneResult.elements[0].primaryLocation.storeNumber == 1
      pageOneResult.elements[0].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[0].locationType.value == "STORE"
      pageOneResult.elements[0].locationType.description == "Store"
   }

   void "fetch by existing barcode" () {
      when:
      def inventory = get("${path}/1104000198")

      then:
      notThrown(HttpClientResponseException)
      inventory.id == 70884
      inventory.serialNumber == "1104000198"
      inventory.lookupKey == "1104000198"
      inventory.lookupKeyType == "BARCODE"
      inventory.barcode == "1104000198"
      inventory.altId == "1104000198"
      inventory.brand == "BLACK & DECKER"
      inventory.modelNumber == "ACB&DBWE12A"
      inventory.productCode == "A-AIR CONDITIONER"
      inventory.description == "AIR CONDITIONER"
      inventory.receivedDate == "2012-05-10"
      inventory.originalCost == 199.99
      inventory.actualCost == 199.99
      inventory.modelCategory == "A"
      inventory.timesRented == 1
      inventory.totalRevenue == 379.99
      inventory.remainingValue == 0.00
      inventory.sellPrice == 379.99
      inventory.assignedValue == 0
      inventory.idleDays == 50
      inventory.condition == null
      inventory.location == null
      inventory.status == "E"
      inventory.primaryLocation.id == 1
      inventory.primaryLocation.storeNumber == 1
      inventory.primaryLocation.name == "KANSAS CITY"
      inventory.locationType.value == "STORE"
      inventory.locationType.description == "Store"
   }
}
