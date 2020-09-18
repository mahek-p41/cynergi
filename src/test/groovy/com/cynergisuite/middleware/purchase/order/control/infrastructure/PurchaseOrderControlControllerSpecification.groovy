package com.cynergisuite.middleware.purchase.order.control.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.control.PurchaseOrderControlDataLoader.PurchaseOrderControlDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class PurchaseOrderControlControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/purchase/order/control'
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject PurchaseOrderControlDataLoaderService purchaseOrderControlDataLoaderService

   void "fetch one purchase order control by company" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)

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
         defaultVendor.id == purchaseOrderControl.defaultVendor.id

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
      response.size() == 1
      response.message == 'Purchase order of the company was unable to be found'
   }

   void "create valid purchase order control" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControl = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      final def jsonPOControl = jsonOutput.toJson(purchaseOrderControl)

      when:
      def result = post("$path/", jsonPOControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
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
         defaultVendor.id == purchaseOrderControl.defaultVendor.id

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
         defaultVendor.id == purchaseOrderControl.defaultVendor.id
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

   void "create valid purchase order control without default vendor" () {
      given:
      final company = nineNineEightEmployee.company

      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControl = purchaseOrderControlDataLoaderService.singleDTO(null, new SimpleIdentifiableDTO(employee.myId()))

      when:
      def result = post("$path/", purchaseOrderControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
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

   void "create valid purchase order control without default approver" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final def purchaseOrderControl = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), null)

      when:
      def result = post("$path/", purchaseOrderControl)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
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
         defaultVendor.id == purchaseOrderControl.defaultVendor.id
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
      final def purchaseOrderControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)

      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControl))

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'company'
      response[0].message == "Purchase order control for user's company " + company.myDataset() + " already exists"

   }

   void "create invalid purchase order control without drop five characters on model number" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('dropFiveCharactersOnModelNumber')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'dropFiveCharactersOnModelNumber'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without update account payable" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('updateAccountPayable')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'updateAccountPayable'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without print second description" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('printSecondDescription')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'printSecondDescription'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without default account payable status type" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('defaultAccountPayableStatusType')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'defaultAccountPayableStatusType'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without print vendor comments" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('printVendorComments')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'printVendorComments'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without include freight in cost" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('includeFreightInCost')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'includeFreightInCost'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without update cost on model" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('updateCostOnModel')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'updateCostOnModel'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control with non-existing vendor id" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.defaultVendor.id = '99'

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'defaultVendor.id'
      response[0].message == '99 was unable to be found'
   }

   void "create invalid purchase order control without update purchase order cost" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('updatePurchaseOrderCost')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'updatePurchaseOrderCost'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without default purchase order type" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('defaultPurchaseOrderType')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'defaultPurchaseOrderType'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without sort by ship to on print" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('sortByShipToOnPrint')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'sortByShipToOnPrint'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without invoice by location" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('invoiceByLocation')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'invoiceByLocation'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control without validate inventory" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('validateInventory')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'validateInventory'
      response[0].message == 'Is required'
   }

   void "create invalid purchase order control with non-existing approver id" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.defaultApprover.id = '0'

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'defaultApprover.id'
      response[0].message == '0 was unable to be found'
   }

   void "create invalid purchase order control without approval required flag type" () {
      given: 'get json PO control and make it invalid'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def purchaseOrderControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      //Make invalid json
      def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(purchaseOrderControlDTO))
      jsonPOControl.remove('approvalRequiredFlagType')

      when:
      post("$path/", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'approvalRequiredFlagType'
      response[0].message == 'Is required'
   }

   void "update valid purchase order control by id" () {
      given: 'Update existingPOControl in db with all new data in jsonPOControl'
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def existingPOControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)
      final def updatedPOControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      final def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(updatedPOControlDTO))
      jsonPOControl.id = existingPOControl.id

      when:
      def result = put("$path/$existingPOControl.id", jsonPOControl)

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
         defaultVendor.id == updatedPOControlDTO.defaultVendor.id

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
         defaultApprover.id == updatedPOControlDTO.defaultApprover.id

         with(approvalRequiredFlagType) {
            value == updatedPOControlDTO.approvalRequiredFlagType.value
            description == updatedPOControlDTO.approvalRequiredFlagType.description
         }
      }
   }

   void "update invalid purchase order control with id 0" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      final def existingPOControl = purchaseOrderControlDataLoaderService.single(company, vendor, employee)
      final def updatedPOControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      final def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(updatedPOControlDTO))
      jsonPOControl.id = '0'

      when:
      put("$path/$existingPOControl.id", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'id'
      response[0].message == 'id must be greater than zero'
   }

   void "update invalid purchase order control with non-existing id" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.single(company)
      final shipViaIn = shipViaTestDataLoaderService.single(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipViaIn)
      final employee = employeeFactoryService.single(company)
      purchaseOrderControlDataLoaderService.single(company, vendor, employee)
      final def updatedPOControlDTO = purchaseOrderControlDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.myId()), new SimpleIdentifiableDTO(employee.myId()))
      final def jsonPOControl = jsonSlurper.parseText(jsonOutput.toJson(updatedPOControlDTO))

      when:
      put("$path/99", jsonPOControl)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'id'
      response[0].message == '99 was unable to be found'
   }
}
