package com.cynergisuite.middleware.purchase.order.control.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.control.PurchaseOrderControlTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class PurchaseOrderControlControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/purchase-order/control'
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject PurchaseOrderControlTestDataLoaderService purchaseOrderControlDataLoaderService

   void "fetch one purchase order control by company" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final purchaseOrderControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)

      when:
      def result = get("$path/")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == purchaseOrderControl.id
         dropFiveCharactersOnModelNumber == purchaseOrderControl.dropFiveCharactersOnModelNumber
         updateAccountPayable == purchaseOrderControl.updateAccountPayable
         printSecondDescription == purchaseOrderControl.printSecondDescription

         with(defaultAccountPayableStatusType) {
            value == purchaseOrderControl.defaultAccountPayableStatusType.value
            description == purchaseOrderControl.defaultAccountPayableStatusType.description
         }

         printVendorComments == purchaseOrderControl.printVendorComments
         includeFreightInCost == purchaseOrderControl.includeFreightInCost
         updateCostOnModel == purchaseOrderControl.updateCostOnModel

         with(defaultVendor) {
            id == purchaseOrderControl.defaultVendor.id
            name == purchaseOrderControl.defaultVendor.name
            address.id == purchaseOrderControl.defaultVendor.address.id
            number == purchaseOrderControl.defaultVendor.number
         }

         with(updatePurchaseOrderCost) {
            value == purchaseOrderControl.updatePurchaseOrderCost.value
            description == purchaseOrderControl.updatePurchaseOrderCost.description
         }

         with(defaultPurchaseOrderType) {
            value == purchaseOrderControl.defaultPurchaseOrderType.value
            description == purchaseOrderControl.defaultPurchaseOrderType.description
         }

         sortByShipToOnPrint == purchaseOrderControl.sortByShipToOnPrint
         invoiceByLocation == purchaseOrderControl.invoiceByLocation
         validateInventory == purchaseOrderControl.validateInventory
         defaultApprover.id == purchaseOrderControl.defaultApprover.id

         with(approvalRequiredFlagType) {
            value == purchaseOrderControl.approvalRequiredFlagType.value
            description == purchaseOrderControl.approvalRequiredFlagType.description
         }
      }
   }

   void "fetch one purchase order control by company not found" () {
      when:
      get("$path/")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == 'Purchase order of the company was unable to be found'
      response.code == 'system.not.found'
   }

   void "create valid purchase order control" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final purchaseOrderControl = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)

      when:
      def result = post("$path/", purchaseOrderControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         dropFiveCharactersOnModelNumber == purchaseOrderControl.dropFiveCharactersOnModelNumber
         updateAccountPayable == purchaseOrderControl.updateAccountPayable
         printSecondDescription == purchaseOrderControl.printSecondDescription

         with(defaultAccountPayableStatusType) {
            value == purchaseOrderControl.defaultAccountPayableStatusType.value
            description == purchaseOrderControl.defaultAccountPayableStatusType.description
         }

         printVendorComments == purchaseOrderControl.printVendorComments
         includeFreightInCost == purchaseOrderControl.includeFreightInCost
         updateCostOnModel == purchaseOrderControl.updateCostOnModel

         with(defaultVendor) {
            id == purchaseOrderControl.defaultVendor.id
            name == purchaseOrderControl.defaultVendor.name
            address.id == purchaseOrderControl.defaultVendor.address.id
            number == purchaseOrderControl.defaultVendor.number
         }

         with(updatePurchaseOrderCost) {
            value == purchaseOrderControl.updatePurchaseOrderCost.value
            description == purchaseOrderControl.updatePurchaseOrderCost.description
         }

         with(defaultPurchaseOrderType) {
            value == purchaseOrderControl.defaultPurchaseOrderType.value
            description == purchaseOrderControl.defaultPurchaseOrderType.description
         }

         sortByShipToOnPrint == purchaseOrderControl.sortByShipToOnPrint
         invoiceByLocation == purchaseOrderControl.invoiceByLocation
         validateInventory == purchaseOrderControl.validateInventory
         defaultApprover.id == employee.id

         with(approvalRequiredFlagType) {
            value == purchaseOrderControl.approvalRequiredFlagType.value
            description == purchaseOrderControl.approvalRequiredFlagType.description
         }
      }

      when:
      def result2 = get("$path/")

      then:
      notThrown(HttpClientResponseException)
      result2 != null
   }

   void "create valid purchase order control without default vendor, default approver" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      def purchaseOrderControl = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)
      purchaseOrderControl.defaultVendor = null
      purchaseOrderControl.defaultApprover = null

      when:
      def result = post("$path/", purchaseOrderControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         dropFiveCharactersOnModelNumber == purchaseOrderControl.dropFiveCharactersOnModelNumber
         updateAccountPayable == purchaseOrderControl.updateAccountPayable
         printSecondDescription == purchaseOrderControl.printSecondDescription

         with(defaultAccountPayableStatusType) {
            value == purchaseOrderControl.defaultAccountPayableStatusType.value
            description == purchaseOrderControl.defaultAccountPayableStatusType.description
         }

         printVendorComments == purchaseOrderControl.printVendorComments
         includeFreightInCost == purchaseOrderControl.includeFreightInCost
         updateCostOnModel == purchaseOrderControl.updateCostOnModel

         with(updatePurchaseOrderCost) {
            value == purchaseOrderControl.updatePurchaseOrderCost.value
            description == purchaseOrderControl.updatePurchaseOrderCost.description
         }

         with(defaultPurchaseOrderType) {
            value == purchaseOrderControl.defaultPurchaseOrderType.value
            description == purchaseOrderControl.defaultPurchaseOrderType.description
         }

         sortByShipToOnPrint == purchaseOrderControl.sortByShipToOnPrint
         invoiceByLocation == purchaseOrderControl.invoiceByLocation
         validateInventory == purchaseOrderControl.validateInventory
         defaultVendor == null
         defaultApprover == null

         with(approvalRequiredFlagType) {
            value == purchaseOrderControl.approvalRequiredFlagType.value
            description == purchaseOrderControl.approvalRequiredFlagType.description
         }
      }

      when:
      def result2 = get("$path/")

      then:
      notThrown(HttpClientResponseException)
      result2 != null
   }

   void "create invalid purchase order control for company with existing record" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final purchaseOrderControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)

      when:
      post("$path/", purchaseOrderControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'company'
      response[0].message == 'coravt already exists'
      response[0].code == 'cynergi.validation.config.exists'

   }

   @Unroll
   void "create invalid purchase order control without #nonNullableProp" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)
      purchaseOrderControlDTO["$nonNullableProp"] = null

      when:
      post("$path/", purchaseOrderControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                     || errorResponsePath
      'dropFiveCharactersOnModelNumber'   || 'dropFiveCharactersOnModelNumber'
      'printSecondDescription'            || 'printSecondDescription'
      'defaultAccountPayableStatusType'   || 'defaultAccountPayableStatusType'
      'printVendorComments'               || 'printVendorComments'
      'includeFreightInCost'              || 'includeFreightInCost'
      'updateCostOnModel'                 || 'updateCostOnModel'
      'updatePurchaseOrderCost'           || 'updatePurchaseOrderCost'
      'defaultPurchaseOrderType'          || 'defaultPurchaseOrderType'
      'sortByShipToOnPrint'               || 'sortByShipToOnPrint'
      'invoiceByLocation'                 || 'invoiceByLocation'
      'validateInventory'                 || 'validateInventory'
      'approvalRequiredFlagType'          || 'approvalRequiredFlagType'
      'updateAccountPayable'              || 'updateAccountPayable'
   }

   void "create invalid purchase order control with non-existing vendor id" () {
      given: 'get json PO control and make it invalid'
      final nonExistentDefaultVendorId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)
      purchaseOrderControlDTO.defaultVendor.id = nonExistentDefaultVendorId

      when:
      post("$path/", purchaseOrderControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'defaultVendor.id'
      response[0].message == "$nonExistentDefaultVendorId was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "create invalid purchase order control with non-existing approver id" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)
      purchaseOrderControlDTO.defaultApprover.id = 0

      when:
      post("$path/", purchaseOrderControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'defaultApprover.id'
      response[0].message == '0 was unable to be found'
   }

   void "update valid purchase order control without default vendor, default approver" () {
      given: 'Update existingPOControl in db with all new data in updatedPOControlDTO'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final existingPOControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)
      final updatedPOControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)
      updatedPOControlDTO.id = existingPOControl.id
      updatedPOControlDTO.defaultVendor = null
      updatedPOControlDTO.defaultApprover = null

      when:
      def result = put("$path/$existingPOControl.id", updatedPOControlDTO)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingPOControl.id
         dropFiveCharactersOnModelNumber == updatedPOControlDTO.dropFiveCharactersOnModelNumber
         updateAccountPayable == updatedPOControlDTO.updateAccountPayable
         printSecondDescription == updatedPOControlDTO.printSecondDescription

         with(defaultAccountPayableStatusType) {
            value == updatedPOControlDTO.defaultAccountPayableStatusType.value
            description == updatedPOControlDTO.defaultAccountPayableStatusType.description
         }

         printVendorComments == updatedPOControlDTO.printVendorComments
         includeFreightInCost == updatedPOControlDTO.includeFreightInCost
         updateCostOnModel == updatedPOControlDTO.updateCostOnModel

         with(updatePurchaseOrderCost) {
            value == updatedPOControlDTO.updatePurchaseOrderCost.value
            description == updatedPOControlDTO.updatePurchaseOrderCost.description
         }

         with(defaultPurchaseOrderType) {
            value == updatedPOControlDTO.defaultPurchaseOrderType.value
            description == updatedPOControlDTO.defaultPurchaseOrderType.description
         }

         sortByShipToOnPrint == updatedPOControlDTO.sortByShipToOnPrint
         invoiceByLocation == updatedPOControlDTO.invoiceByLocation
         validateInventory == updatedPOControlDTO.validateInventory
         defaultVendor == null
         defaultApprover == null

         with(approvalRequiredFlagType) {
            value == updatedPOControlDTO.approvalRequiredFlagType.value
            description == updatedPOControlDTO.approvalRequiredFlagType.description
         }
      }
   }

   void "update invalid purchase order control with non-existing default vendor, default approver" () {
      given: 'Update existingPOControl in db with all new data in jsonPOControl'
      final vendorId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final existingPOControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)
      final updatedPOControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)
      updatedPOControlDTO.defaultVendor.id = vendorId
      updatedPOControlDTO.defaultApprover.id = 999

      when:
      put("$path/$existingPOControl.id", updatedPOControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 2
      response[0].path == 'defaultApprover.id'
      response[0].message == '999 was unable to be found'
      response[1].path == 'defaultVendor.id'
      response[1].message == "${vendorId} was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "update invalid purchase order control with id" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final existingPOControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)
      final updatedPOControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)
      updatedPOControlDTO.id = UUID.randomUUID() // this should be ignored by the API

      when:
      def response = put("$path/$existingPOControl.id", updatedPOControlDTO)

      then:
      notThrown(HttpClientResponseException)
      with(response) {
         id == existingPOControl.id
         dropFiveCharactersOnModelNumber == updatedPOControlDTO.dropFiveCharactersOnModelNumber
         updateAccountPayable == updatedPOControlDTO.updateAccountPayable
         printSecondDescription == updatedPOControlDTO.printSecondDescription

         with(defaultAccountPayableStatusType) {
            value == updatedPOControlDTO.defaultAccountPayableStatusType.value
            description == updatedPOControlDTO.defaultAccountPayableStatusType.description
         }

         printVendorComments == updatedPOControlDTO.printVendorComments
         includeFreightInCost == updatedPOControlDTO.includeFreightInCost
         updateCostOnModel == updatedPOControlDTO.updateCostOnModel

         with(updatePurchaseOrderCost) {
            value == updatedPOControlDTO.updatePurchaseOrderCost.value
            description == updatedPOControlDTO.updatePurchaseOrderCost.description
         }

         with(defaultPurchaseOrderType) {
            value == updatedPOControlDTO.defaultPurchaseOrderType.value
            description == updatedPOControlDTO.defaultPurchaseOrderType.description
         }

         sortByShipToOnPrint == updatedPOControlDTO.sortByShipToOnPrint
         invoiceByLocation == updatedPOControlDTO.invoiceByLocation
         validateInventory == updatedPOControlDTO.validateInventory
         defaultVendor.id == updatedPOControlDTO.defaultVendor.id
         defaultApprover.id == updatedPOControlDTO.defaultApprover.id

         with(approvalRequiredFlagType) {
            value == updatedPOControlDTO.approvalRequiredFlagType.value
            description == updatedPOControlDTO.approvalRequiredFlagType.description
         }
      }
   }

   void "update invalid purchase order control with non-existing id" () {
      given:
      final invalidPurchaseOrderId = UUID.randomUUID()
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      purchaseOrderControlDataLoaderService.single(company, vendor, employee)
      final updatedPOControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)

      when:
      put("$path/$invalidPurchaseOrderId", updatedPOControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'id'
      response[0].message == "$invalidPurchaseOrderId was unable to be found"
      response[0].code == 'system.not.found'
   }

   void "update invalid purchase order control with non-nullable properties" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final purchaseOrder = purchaseOrderControlDataLoaderService.single(company, vendor, employee)
      def updatedPOControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new VendorDTO(vendor), employee)
      updatedPOControlDTO.dropFiveCharactersOnModelNumber = null
      updatedPOControlDTO.updateAccountPayable = null
      updatedPOControlDTO.printSecondDescription = null
      updatedPOControlDTO.defaultAccountPayableStatusType = null
      updatedPOControlDTO.printVendorComments = null
      updatedPOControlDTO.includeFreightInCost = null
      updatedPOControlDTO.updateCostOnModel = null
      updatedPOControlDTO.updatePurchaseOrderCost = null
      updatedPOControlDTO.defaultPurchaseOrderType = null
      updatedPOControlDTO.sortByShipToOnPrint = null
      updatedPOControlDTO.invoiceByLocation = null
      updatedPOControlDTO.validateInventory = null
      updatedPOControlDTO.approvalRequiredFlagType = null

      when:
      put("$path/$purchaseOrder.id", updatedPOControlDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson().collect().sort { a,b -> a.path <=> b.path }
      response.size() == 13
      response[0].path == 'approvalRequiredFlagType'
      response[1].path == 'defaultAccountPayableStatusType'
      response[2].path == 'defaultPurchaseOrderType'
      response[3].path == 'dropFiveCharactersOnModelNumber'
      response[4].path == 'includeFreightInCost'
      response[5].path == 'invoiceByLocation'
      response[6].path == 'printSecondDescription'
      response[7].path == 'printVendorComments'
      response[8].path == 'sortByShipToOnPrint'
      response[9].path == 'updateAccountPayable'
      response[10].path == 'updateCostOnModel'
      response[11].path == 'updatePurchaseOrderCost'
      response[12].path == 'validateInventory'
      response.collect { it.message } as Set == ['Is required'] as Set
   }
}
