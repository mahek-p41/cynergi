package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

@MicronautTest(transactional = false)
class InventoryControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/inventory/all"

   void "fetch first page without locationType" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'tstds1', 1).blockingGet()
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: tstds1Store1User.myLocation().myNumber(), inventoryStatus: ["N", "O", "R", "D"]])

      when:
      def pageOneResult = get("${path}${pageOne}&extraParamter=one&exParamTwo=2", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.locationType == null
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.inventoryStatus == ["R", "D", "N", "O"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 423
      pageOneResult.totalPages == 85
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == 73821
      pageOneResult.elements[0].serialNumber == null
      pageOneResult.elements[0].barcode == null
      pageOneResult.elements[0].lookupKey == "201-00923"
      pageOneResult.elements[0].lookupKeyType == "ALT_ID"
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

   void "fetch first page of inventory-app without locationType" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'tstds1', 1).blockingGet()
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: tstds1Store1User.myLocation().myNumber(), inventoryStatus: ["N", "O", "R", "D"]])

      when:
      def pageOneResult = get("inventory/${pageOne}&extraParamter=one&exParamTwo=2", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      with(pageOneResult) {
         locationType == null
         requested.storeNumber == tstds1Store1User.myLocation().myNumber()
         requested.inventoryStatus == ["R", "D", "N", "O"]
         elements != null
         elements.size() == 5
         totalElements == 423
         totalPages == 85
         first == true
         last == false
         elements[0].id == 73821
         elements[0].serialNumber == null
         elements[0].barcode == null
         elements[0].altId == "201-00923"
         elements[0].lookupKey == "201-00923"
         elements[0].modelNumber == "FPGIDFRAMEDART"
         elements[0].description == "MISCELLANEOUS FURNITURE PICT"
         // below this values should be null
         elements[0].lookupKeyType == 'ALT_ID'
         elements[0].originalCost == 55.03
         elements[0].actualCost == 55.03
         elements[0].modelCategory == "F"
         elements[0].timesRented == 0
         elements[0].totalRevenue == 0
         elements[0].remainingValue == 0
         elements[0].sellPrice == 0
         elements[0].assignedValue == 0
         elements[0].idleDays == 0
      }
   }

   void "fetch first page with locationType" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'tstds1', 1).blockingGet()
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]])

      when:
      def pageOneResult = get("${path}${pageOne}", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.locationType == "STORE"
      pageOneResult.requested.inventoryStatus == ["R", "D", "N", "O"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 423
      pageOneResult.totalPages == 85
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[1].id == 73823
      pageOneResult.elements[1].serialNumber == null
      pageOneResult.elements[1].barcode == null
      pageOneResult.elements[1].altId == "201-00925"
      pageOneResult.elements[1].lookupKey == "201-00925"
      pageOneResult.elements[1].lookupKeyType == "ALT_ID"
      pageOneResult.elements[1].brand == "GIDGET MEAUT"
      pageOneResult.elements[1].modelNumber == "FPGIDFRAMEDART"
      pageOneResult.elements[1].productCode == "F-MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[1].description == "MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[1].receivedDate == "2012-04-13"
      pageOneResult.elements[1].originalCost == 55.03
      pageOneResult.elements[1].actualCost == 55.03
      pageOneResult.elements[1].modelCategory == "F"
      pageOneResult.elements[1].timesRented == 0
      pageOneResult.elements[1].totalRevenue == 0.00
      pageOneResult.elements[1].remainingValue == 0.00
      pageOneResult.elements[1].sellPrice == 0.00
      pageOneResult.elements[1].assignedValue == 0.00
      pageOneResult.elements[1].idleDays == 0
      pageOneResult.elements[1].condition == null
      pageOneResult.elements[1].status == "N"
      pageOneResult.elements[1].primaryLocation.id == 1
      pageOneResult.elements[1].primaryLocation.storeNumber == 1
      pageOneResult.elements[1].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[1].locationType.value == "STORE"
      pageOneResult.elements[1].locationType.description == "Store"
   }

   void "fetch first page of inventory with status of N" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'tstds1', 1).blockingGet()
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: tstds1Store1User.myLocation().myNumber(), inventoryStatus: ["N"]])

      when:
      def pageOneResult = get("${path}${pageOne}", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.inventoryStatus.size() == 1
      pageOneResult.requested.inventoryStatus == ["N"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 252
      pageOneResult.totalPages == 51
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[2].id == 73829
      pageOneResult.elements[2].serialNumber == "201-00931"
      pageOneResult.elements[2].lookupKey == "201-00931"
      pageOneResult.elements[2].lookupKeyType == "SERIAL"
      pageOneResult.elements[2].barcode == "201-00931"
      pageOneResult.elements[2].altId == null
      pageOneResult.elements[2].brand == "GIDGET MEAUT"
      pageOneResult.elements[2].modelNumber == "FPGIDFRAMEDART"
      pageOneResult.elements[2].productCode == "F-MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[2].description == "MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[2].receivedDate == "2012-04-13"
      pageOneResult.elements[2].originalCost == 55.03
      pageOneResult.elements[2].actualCost == 55.03
      pageOneResult.elements[2].modelCategory == "F"
      pageOneResult.elements[2].timesRented == 0
      pageOneResult.elements[2].totalRevenue == 0.00
      pageOneResult.elements[2].remainingValue == 0.00
      pageOneResult.elements[2].sellPrice == 0.00
      pageOneResult.elements[2].assignedValue == 0.00
      pageOneResult.elements[2].idleDays == 0
      pageOneResult.elements[2].condition == null
      pageOneResult.elements[2].status == "N"
      pageOneResult.elements[2].primaryLocation.id == 1
      pageOneResult.elements[2].primaryLocation.storeNumber == 1
      pageOneResult.elements[2].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[2].locationType.value == "STORE"
      pageOneResult.elements[2].locationType.description == "Store"
   }

   void "fetch by location type store"() {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'tstds1', 1).blockingGet()
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: tstds1Store1User.myLocation().myNumber(), locationType: "STORE", inventoryStatus: ["N", "O", "R", "D"]])

      when:
      def pageOneResult = get("${path}${pageOne}", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.inventoryStatus == ["R", "D", "N", "O"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 423
      pageOneResult.totalPages == 85
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[3].id == 73830
      pageOneResult.elements[3].serialNumber == "00110360"
      pageOneResult.elements[3].barcode == "00110360"
      pageOneResult.elements[3].altId == null
      pageOneResult.elements[3].lookupKey == "00110360"
      pageOneResult.elements[3].lookupKeyType == "SERIAL"
      pageOneResult.elements[3].brand == "HD CANVAS ART"
      pageOneResult.elements[3].modelNumber == "FPHAD11856"
      pageOneResult.elements[3].productCode == "F-MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[3].description == "MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[3].receivedDate == "2010-05-18"
      pageOneResult.elements[3].originalCost == 85.00
      pageOneResult.elements[3].actualCost == 85.00
      pageOneResult.elements[3].modelCategory == "F"
      pageOneResult.elements[3].timesRented == 0
      pageOneResult.elements[3].totalRevenue == 0.00
      pageOneResult.elements[3].remainingValue == 0.00
      pageOneResult.elements[3].sellPrice == 0.00
      pageOneResult.elements[3].assignedValue == 0.00
      pageOneResult.elements[3].idleDays == 0
      pageOneResult.elements[3].condition == null
      pageOneResult.elements[3].status == "N"
      pageOneResult.elements[3].primaryLocation.id == 1
      pageOneResult.elements[3].primaryLocation.storeNumber == 1
      pageOneResult.elements[3].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[3].locationType.value == "STORE"
      pageOneResult.elements[3].locationType.description == "Store"
   }

   void "fetch by existing barcode" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'tstds1', 1).blockingGet()
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)

      when:
      def inventory = get("inventory/201-00923", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      with(inventory) {
         id == 73821
         serialNumber == null
         barcode == null
         lookupKey == "201-00923"
         lookupKeyType == "ALT_ID"
         altId == "201-00923"
         brand == "GIDGET MEAUT"
         modelNumber == "FPGIDFRAMEDART"
         productCode == "F-MISCELLANEOUS FURNITURE PICT"
         description == "MISCELLANEOUS FURNITURE PICT"
         receivedDate == "2012-04-13"
         originalCost == 55.03
         actualCost == 55.03
         modelCategory == "F"
         timesRented == 0
         totalRevenue == 0.00
         remainingValue == 0.00
         sellPrice == 0.00
         assignedValue == 0.00
         idleDays == 0
         condition == null
         status == "N"
         primaryLocation.id == 1
         primaryLocation.storeNumber == 1
         primaryLocation.name == "KANSAS CITY"
         locationType.value == "STORE"
         locationType.description == "Store"
      }
   }

   void "fetch by existing lookup key" () {
      when:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'tstds1', 1).blockingGet()
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      def inventory = get("inventory/lookup?key=00110360", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      with(inventory) {
         id == 73830
         serialNumber == "00110360"
         lookupKey == "00110360"
         lookupKeyType == "SERIAL"
         barcode == "00110360"
         altId == null
         brand == "HD CANVAS ART"
         modelNumber == "FPHAD11856"
         productCode == "F-MISCELLANEOUS FURNITURE PICT"
         description == "MISCELLANEOUS FURNITURE PICT"
         receivedDate == "2010-05-18"
         originalCost == 85
         actualCost == 85
         modelCategory == "F"
         timesRented == 0
         totalRevenue == 0
         remainingValue == 0
         sellPrice == 0
         assignedValue == 0
         idleDays == 0
         condition == null
         with(location) {
            id == 1
            storeNumber == 1
            name == "KANSAS CITY"
         }
         status == "N"
         with(primaryLocation) {
            id == 1
            storeNumber == 1
            name == "KANSAS CITY"
         }
         with(locationType) {
            value == "STORE"
            description == "Store"
         }
      }
   }
}
