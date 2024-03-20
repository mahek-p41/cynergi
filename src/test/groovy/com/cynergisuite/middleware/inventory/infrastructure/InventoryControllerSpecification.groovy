package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.InventoryInquiryFilterRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Ignore

import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class InventoryControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/inventory/all"
   private static final String mobilePath = "/inventory"

   void "fetch first page without locationType" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(1082, '1992', 'coravt', 1)
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: tstds1Store1User.myLocation().myNumber(), inventoryStatus: ["R", "D", "N"]])

      when:
      def pageOneResult = get("${path}${pageOne}&extraParamter=one&exParamTwo=2", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.locationType == null
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.inventoryStatus == ["R", "D", "N"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 1694
      pageOneResult.totalPages == 339
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[0].id == UUID.fromString("6d7250a8-e6c8-11ee-b0ca-0242ac130004")

   }

   void "fetch first page of inventory-app without locationType" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(1082, '1992', 'coravt', 1)
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: tstds1Store1User.myLocation().myNumber(), inventoryStatus: ["R", "D", "N"]])

      when:
      def pageOneResult = get("inventory/${pageOne}&extraParamter=one&exParamTwo=2", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      with(pageOneResult) {
         locationType == null
         requested.storeNumber == tstds1Store1User.myLocation().myNumber()
         requested.inventoryStatus == ["R", "D", "N"]
         elements != null
         elements.size() == 5
         totalElements == 1694
         totalPages == 339
         first == true
         last == false
         elements[0].id == UUID.fromString("6d7250a8-e6c8-11ee-b0ca-0242ac130004")
         elements[0].lookupKey == "00127405"
         elements[0].modelNumber == "TR"
         elements[0].description == "19 IN TIRE"
         elements[0].serialNumber != null
         elements[0].barcode != null
         // below this values should be null
         elements[0].altId == null
         elements[0].lookupKeyType == null
         elements[0].originalCost == null
         elements[0].actualCost == null
         elements[0].modelCategory == null
         elements[0].timesRented == null
         elements[0].totalRevenue == null
         elements[0].remainingValue == null
         elements[0].sellPrice == null
         elements[0].assignedValue == null
         elements[0].idleDays == null
      }
   }

   void "fetch first page with locationType" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(1082, '1992', 'coravt', 1)
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", locationType: "STORE", inventoryStatus: ["R", "D", "N"]])

      when:
      def pageOneResult = get("${path}${pageOne}", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.locationType == "STORE"
      pageOneResult.requested.inventoryStatus == ["R", "D", "N"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 1694
      pageOneResult.totalPages == 339
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[1].id == UUID.fromString("6d729388-e6c8-11ee-b0ca-0242ac130004")
      pageOneResult.elements[1].primaryLocation.id == 2
      pageOneResult.elements[1].primaryLocation.storeNumber == 1
      pageOneResult.elements[1].primaryLocation.name == "HOUMA"
      pageOneResult.elements[1].locationType.value == "STORE"
      pageOneResult.elements[1].locationType.description == "Store"
   }

   @Ignore
   // Unfortunately, this test can not make the real request to test the real issues
   // Use postman to make a mobile-app-like-request for a reliable test result
   void "mobile-app-like-request fetch first page with locationType" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'coravt', 1)
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 100, locationType: "STORE"])

      when:
      def pageOneResult = get("${mobilePath}${pageOne}", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.locationType == "STORE"
      pageOneResult.requested.inventoryStatus == ["R", "D", "N"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 100
      pageOneResult.totalElements == 1694
      pageOneResult.totalPages == 5
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[1].id == 73823
      pageOneResult.elements[1].lookupKey == "201-00925"
      pageOneResult.elements[1].modelNumber == "FPGIDFRAMEDART"
      pageOneResult.elements[1].description == "MISCELLANEOUS FURNITURE PICT"
      pageOneResult.elements[1].serialNumber == null
      pageOneResult.elements[1].barcode == null
      pageOneResult.elements[1].altId == null
      pageOneResult.elements[1].lookupKeyType == null
      pageOneResult.elements[1].brand == null
      pageOneResult.elements[1].productCode == null
      pageOneResult.elements[1].receivedDate == null
      pageOneResult.elements[1].originalCost == null
      pageOneResult.elements[1].actualCost == null
      pageOneResult.elements[1].modelCategory == null
      pageOneResult.elements[1].timesRented == null
      pageOneResult.elements[1].totalRevenue == null
      pageOneResult.elements[1].remainingValue == null
      pageOneResult.elements[1].sellPrice == null
      pageOneResult.elements[1].assignedValue == null
      pageOneResult.elements[1].idleDays == null
      pageOneResult.elements[1].condition == null
      pageOneResult.elements[1].status == null
      pageOneResult.elements[1].primaryLocation == null
      pageOneResult.elements[1].locationType == null
   }

   void "fetch first page of inventory with status of N" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(1082, '1992', 'coravt', 1)
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
      pageOneResult.totalElements == 700
      pageOneResult.totalPages == 140
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[2].id == UUID.fromString("6d7324c4-e6c8-11ee-b0ca-0242ac130004")
      pageOneResult.elements[2].serialNumber == "10000865"
      pageOneResult.elements[2].lookupKey == "10000865"
      pageOneResult.elements[2].lookupKeyType == "SERIAL"
      pageOneResult.elements[2].barcode == "10000865"
      pageOneResult.elements[2].altId == "10000865"
      pageOneResult.elements[2].brand == "LEXANI"
      pageOneResult.elements[2].modelNumber == "TR"
      pageOneResult.elements[2].productCode == "T-22 IN TIRE"
      pageOneResult.elements[2].description == "22 IN TIRE"
      pageOneResult.elements[2].receivedDate == "2017-04-05"
      pageOneResult.elements[2].originalCost == 103.00
      pageOneResult.elements[2].actualCost == 92.26
      pageOneResult.elements[2].modelCategory == "T"
      pageOneResult.elements[2].timesRented == 0
      pageOneResult.elements[2].totalRevenue == 0.00
      pageOneResult.elements[2].remainingValue == 92.26
      pageOneResult.elements[2].sellPrice == 0.00
      pageOneResult.elements[2].assignedValue == 0.00
      pageOneResult.elements[2].idleDays == 0
      pageOneResult.elements[2].condition == null
      pageOneResult.elements[2].status == "N"
      pageOneResult.elements[2].primaryLocation.id == 2
      pageOneResult.elements[2].primaryLocation.storeNumber == 1
      pageOneResult.elements[2].primaryLocation.name == "HOUMA"
      pageOneResult.elements[2].locationType.value == "STORE"
      pageOneResult.elements[2].locationType.description == "Store"
   }

   @Ignore
   void "fetch first page of inventory with status of D" () {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(100, 'pass', 'coravt', 1)
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: tstds1Store1User.myLocation().myNumber(), inventoryStatus: ["D"]])

      when:
      def pageOneResult = get("${path}${pageOne}", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.inventoryStatus.size() == 1
      pageOneResult.requested.inventoryStatus == ["D"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 4
      pageOneResult.totalElements == 4
      pageOneResult.totalPages == 1
      pageOneResult.first == true
      pageOneResult.last == true
      pageOneResult.elements[2].id == 85324
      pageOneResult.elements[2].serialNumber == "3CUK902302"
      pageOneResult.elements[2].lookupKey == "3CUK902302"
      pageOneResult.elements[2].lookupKeyType == "ALT_ID"
      pageOneResult.elements[2].barcode == "3CUK902302"
      pageOneResult.elements[2].altId == "3CUK902302"
      pageOneResult.elements[2].brand == "SAMSUNG"
      pageOneResult.elements[2].modelNumber == "TTSAMUN58NU710DXNA"
      pageOneResult.elements[2].productCode == "T-TV 58 SMART"
      pageOneResult.elements[2].description == "TV 58 SMART"
      pageOneResult.elements[2].receivedDate == "2018-11-21"
      pageOneResult.elements[2].originalCost == 479.00
      pageOneResult.elements[2].actualCost == 479.00
      pageOneResult.elements[2].modelCategory == "T"
      pageOneResult.elements[2].timesRented == 0
      pageOneResult.elements[2].totalRevenue == 0.00
      pageOneResult.elements[2].remainingValue == 479.00
      pageOneResult.elements[2].sellPrice == 0.00
      pageOneResult.elements[2].assignedValue == 0.00
      pageOneResult.elements[2].idleDays == 0
      pageOneResult.elements[2].condition == null
      pageOneResult.elements[2].status == "D"
      pageOneResult.elements[2].primaryLocation.id == 1
      pageOneResult.elements[2].primaryLocation.storeNumber == 1
      pageOneResult.elements[2].primaryLocation.name == "KANSAS CITY"
      pageOneResult.elements[2].locationType.value == "STORE"
      pageOneResult.elements[2].locationType.description == "Store"
   }

   void "fetch by location type store"() {
      given:
      final tstds1Store1User = userService.fetchUserByAuthentication(1082, '1992', 'coravt', 1)
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      final pageOne = new InventoryPageRequest([page: 1, size: 5, sortBy: "id", sortDirection: "ASC", storeNumber: tstds1Store1User.myLocation().myNumber(), locationType: "STORE", inventoryStatus: ["R", "D", "N"]])

      when:
      def pageOneResult = get("${path}${pageOne}", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      pageOneResult.requested.storeNumber == tstds1Store1User.myLocation().myNumber()
      pageOneResult.requested.inventoryStatus == ["R", "D", "N"]
      pageOneResult.elements != null
      pageOneResult.elements.size() == 5
      pageOneResult.totalElements == 1694
      pageOneResult.totalPages == 339
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements[3].id == UUID.fromString("6d72e298-e6c8-11ee-b0ca-0242ac130004")
      pageOneResult.elements[3].serialNumber == "00128336"
      pageOneResult.elements[3].barcode == "00128336"
      pageOneResult.elements[3].altId == "00128336"
      pageOneResult.elements[3].lookupKey == "00128336"
      pageOneResult.elements[3].lookupKeyType == "SERIAL"
      pageOneResult.elements[3].brand == "A-PLUS"
      pageOneResult.elements[3].modelNumber == "TR"
      pageOneResult.elements[3].productCode == "T-16 IN TIRE"
      pageOneResult.elements[3].description == "16 IN TIRE"
      pageOneResult.elements[3].receivedDate == "2016-01-22"
      pageOneResult.elements[3].originalCost == 48.07
      pageOneResult.elements[3].actualCost == 48.07
      pageOneResult.elements[3].modelCategory == "T"
      pageOneResult.elements[3].timesRented == 1
      pageOneResult.elements[3].totalRevenue == 220.85
      pageOneResult.elements[3].remainingValue == 19.34
      pageOneResult.elements[3].sellPrice == 170.60
      pageOneResult.elements[3].assignedValue == 0.00
      pageOneResult.elements[3].idleDays == 0
      pageOneResult.elements[3].condition == "OKAY"
      pageOneResult.elements[3].status == "R"
      pageOneResult.elements[3].primaryLocation.id == 2
      pageOneResult.elements[3].primaryLocation.storeNumber == 1
      pageOneResult.elements[3].primaryLocation.name == "HOUMA"
      pageOneResult.elements[3].locationType.value == "STORE"
      pageOneResult.elements[3].locationType.description == "Store"
   }

   void "fetch by existing lookup key" () {
      when:
      final tstds1Store1User = userService.fetchUserByAuthentication(1082, '1992', 'coravt', 1)
      final tstds1Store1UserLogin = loginEmployee(tstds1Store1User)
      def inventory = get("inventory/lookup?key=00136726", tstds1Store1UserLogin)

      then:
      notThrown(HttpClientResponseException)
      with(inventory) {
         id == UUID.fromString("6d7fabb8-e6c8-11ee-b0ca-0242ac130004")
      }
   }

   void "fetch AP inventory inquiry" () {
      given:
      def filterRequest = new InventoryInquiryFilterRequest()
      switch (criteria) {
         case 'Search by serial number':
            filterRequest['serialNbr'] = '10005676'
            filterRequest['sortBy'] = 'inv.serial_number'
            break
         case 'Search by po number':
            filterRequest['poNbr'] = 'S100000703'
            filterRequest['sortBy'] = 'inv.inv_purchase_order_number'
            break
         case 'Search by alternate id':
            filterRequest['beginAltId'] = '10004640'
            filterRequest['endAltId'] = '10005019'
            filterRequest['sortBy'] = 'inv.alternate_id'
            break
      }

      when:
      def result = get("/inventory/inquiry${filterRequest}")

      then:
      notThrown(Exception)
      result != null
      result.totalElements == elements

      where:
      criteria                         || elements
      'Search by serial number'        || 1
      'Search by po number'            || 4
      'Search by alternate id'         || 271
   }

   void "fetch AP inventory inquiry no content" () {
      given:
      def filterRequest = new InventoryInquiryFilterRequest()
      filterRequest['sortBy'] = 'inv.inv_purchase_order_number'
      filterRequest['recvLoc'] = 10001

      when:
      get("/inventory/inquiry${filterRequest}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NO_CONTENT
   }
}
