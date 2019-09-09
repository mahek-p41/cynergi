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
      pageOneResult.requested.inventoryStatus == ["N", "O", "R", "D"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 1134
      pageOneResult.totalPages == 227
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 2
      pageOneResult.elements[0].serialNumber == "A973V0167"
      pageOneResult.elements[0].lookupKey == "A973V0167"
      pageOneResult.elements[0].lookupKeyType == "SERIAL"
      pageOneResult.elements[0].barcode == "A973V0167"
      pageOneResult.elements[0].altId == "00110411"
      pageOneResult.elements[0].brand == "AMANA"
      pageOneResult.elements[0].modelNumber == "ACAMAAMACD125R"
      pageOneResult.elements[0].productCode == "A-AIR CONDITIONER"
      pageOneResult.elements[0].description == "AIR CONDITIONER"
      pageOneResult.elements[0].receivedDate == "2010-06-08"
      pageOneResult.elements[0].originalCost == 245
      pageOneResult.elements[0].actualCost == 245
      pageOneResult.elements[0].modelCategory == "A"
      pageOneResult.elements[0].timesRented == 5
      pageOneResult.elements[0].totalRevenue == 793.66
      pageOneResult.elements[0].remainingValue == 0
      pageOneResult.elements[0].sellPrice == 171.46
      pageOneResult.elements[0].assignedValue == 0
      pageOneResult.elements[0].idleDays == 14
      pageOneResult.elements[0].condition == "FAIR"
      pageOneResult.elements[0].status == "O"
      pageOneResult.elements[0].primaryLocation.id == 1
      pageOneResult.elements[0].primaryLocation.storeNumber == 1
      pageOneResult.elements[0].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[0].locationType.value == "CUSTOMER"
      pageOneResult.elements[0].locationType.description == "Customer"
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
      pageOneResult.requested.inventoryStatus == ["N", "O", "R", "D"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 392
      pageOneResult.totalPages == 79
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 188
      pageOneResult.elements[0].serialNumber == "201-01133"
      pageOneResult.elements[0].lookupKey == "201-01133"
      pageOneResult.elements[0].lookupKeyType == "BARCODE"
      pageOneResult.elements[0].barcode == "201-01133"
      pageOneResult.elements[0].altId == "201-01133"
      pageOneResult.elements[0].brand == "PERDUE"
      pageOneResult.elements[0].modelNumber == "BCPER49324"
      pageOneResult.elements[0].productCode == "B-BEDROOM CHEST"
      pageOneResult.elements[0].description == "BEDROOM CHEST"
      pageOneResult.elements[0].receivedDate == "2012-06-11"
      pageOneResult.elements[0].originalCost == 90
      pageOneResult.elements[0].actualCost == 90
      pageOneResult.elements[0].modelCategory == "B"
      pageOneResult.elements[0].timesRented == 0
      pageOneResult.elements[0].totalRevenue == 0
      pageOneResult.elements[0].remainingValue == 86.25
      pageOneResult.elements[0].sellPrice == 0
      pageOneResult.elements[0].assignedValue == 0
      pageOneResult.elements[0].idleDays == 0
      pageOneResult.elements[0].condition == null
      pageOneResult.elements[0].location.id == 1
      pageOneResult.elements[0].location.storeNumber == 1
      pageOneResult.elements[0].location.name == "KANSAS CITY"
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
      pageOneResult.totalElements == 269
      pageOneResult.totalPages == 54
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 188
      pageOneResult.elements[0].serialNumber == "201-01133"
      pageOneResult.elements[0].lookupKey == "201-01133"
      pageOneResult.elements[0].lookupKeyType == "BARCODE"
      pageOneResult.elements[0].barcode == "201-01133"
      pageOneResult.elements[0].altId == "201-01133"
      pageOneResult.elements[0].brand == "PERDUE"
      pageOneResult.elements[0].modelNumber == "BCPER49324"
      pageOneResult.elements[0].productCode == "B-BEDROOM CHEST"
      pageOneResult.elements[0].description == "BEDROOM CHEST"
      pageOneResult.elements[0].receivedDate == "2012-06-11"
      pageOneResult.elements[0].originalCost == 90
      pageOneResult.elements[0].actualCost == 90
      pageOneResult.elements[0].modelCategory == "B"
      pageOneResult.elements[0].timesRented == 0
      pageOneResult.elements[0].totalRevenue == 0
      pageOneResult.elements[0].remainingValue == 86.25
      pageOneResult.elements[0].sellPrice == 0
      pageOneResult.elements[0].assignedValue == 0
      pageOneResult.elements[0].idleDays == 0
      pageOneResult.elements[0].condition == null
      pageOneResult.elements[0].location.id == 1
      pageOneResult.elements[0].location.storeNumber == 1
      pageOneResult.elements[0].location.name == "KANSAS CITY"
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
      pageOneResult.requested.inventoryStatus == ["N", "O", "R", "D"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 392
      pageOneResult.totalPages == 79
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 188
      pageOneResult.elements[0].serialNumber == "201-01133"
      pageOneResult.elements[0].lookupKey == "201-01133"
      pageOneResult.elements[0].lookupKeyType == "BARCODE"
      pageOneResult.elements[0].barcode == "201-01133"
      pageOneResult.elements[0].altId == "201-01133"
      pageOneResult.elements[0].brand == "PERDUE"
      pageOneResult.elements[0].modelNumber == "BCPER49324"
      pageOneResult.elements[0].productCode == "B-BEDROOM CHEST"
      pageOneResult.elements[0].description == "BEDROOM CHEST"
      pageOneResult.elements[0].receivedDate == "2012-06-11"
      pageOneResult.elements[0].originalCost == 90
      pageOneResult.elements[0].actualCost == 90
      pageOneResult.elements[0].modelCategory == "B"
      pageOneResult.elements[0].timesRented == 0
      pageOneResult.elements[0].totalRevenue == 0
      pageOneResult.elements[0].remainingValue == 86.25
      pageOneResult.elements[0].sellPrice == 0
      pageOneResult.elements[0].assignedValue == 0
      pageOneResult.elements[0].idleDays == 0
      pageOneResult.elements[0].condition == null
      pageOneResult.elements[0].location.id == 1
      pageOneResult.elements[0].location.storeNumber == 1
      pageOneResult.elements[0].location.name == "KANSAS CITY"
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
      inventory.id == 9
      inventory.serialNumber == "1104000198"
      inventory.lookupKey == "1104000198"
      inventory.lookupKeyType == "SERIAL"
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
      inventory.totalRevenue == 62.83
      inventory.remainingValue == 183.32
      inventory.sellPrice == 379.99
      inventory.assignedValue == 0
      inventory.idleDays == 50
      inventory.condition == null
      inventory.location == null
      inventory.status == "O"
      inventory.primaryLocation.id == 1
      inventory.primaryLocation.storeNumber == 1
      inventory.primaryLocation.name == "KANSAS CITY"
      inventory.locationType.value == "CUSTOMER"
      inventory.locationType.description == "Customer"
   }

   void "fetch all pages using locationType of STORE" () {

   }
}
