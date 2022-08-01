package com.cynergisuite.middleware.purchase.order.detail.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.purchase.order.detail.PurchaseOrderDetailDTO
import com.cynergisuite.middleware.purchase.order.detail.PurchaseOrderDetailDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class PurchaseOrderDetailControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/purchase-order/detail"

   @Inject PurchaseOrderTestDataLoaderService purchaseOrderTestDataLoaderService
   @Inject PurchaseOrderDetailDataLoaderService purchaseOrderDetailTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService

   void "fetch one" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia as ShipViaEntity)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia as ShipViaEntity)
      final poDetail = purchaseOrderDetailTestDataLoaderService.single(company, purchaseOrderIn, shipToIn, vendorIn)

      when:
      def result = get("$path/${poDetail.id}")

      then:
      notThrown(HttpClientResponseException)
      with(result) {
         id == poDetail.id
         number == poDetail.number
         purchaseOrder.id == poDetail.purchaseOrder.id
         itemfileNumber == poDetail.itemfileNumber
         orderQuantity == poDetail.orderQuantity
         receivedQuantity == poDetail.receivedQuantity
         cost == poDetail.cost
         message == poDetail.message
         color == poDetail.color
         fabric == poDetail.fabric
         cancelledQuantity == poDetail.cancelledQuantity
         cancelledTempQuantity == poDetail.cancelledTempQuantity
         shipTo.id == poDetail.shipTo.myId()
         requiredDate == poDetail.requiredDate.toString()
         dateOrdered == poDetail.dateOrdered.toString()
         freightPerItem == poDetail.freightPerItem
         tempQuantityToReceive == poDetail.tempQuantityToReceive
         vendor.id == poDetail.vendor.id
         lastReceivedDate == poDetail.lastReceivedDate.toString()
         landedCost == poDetail.landedCost

         with(statusType) {
            value == poDetail.statusType.value
            description == poDetail.statusType.description
         }

         with(purchaseOrderRequisitionIndicatorType) {
            value == poDetail.purchaseOrderRequisitionIndicatorType.value
            description == poDetail.purchaseOrderRequisitionIndicatorType.description
         }

         with(exceptionIndicatorType) {
            value == poDetail.exceptionIndicatorType.value
            description == poDetail.exceptionIndicatorType.description
         }

         convertedPurchaseOrderNumber == poDetail.convertedPurchaseOrderNumber
         approvedIndicator == poDetail.approvedIndicator
      }
   }

   void "fetch one not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("$path/$nonExistentId")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia as ShipViaEntity)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia as ShipViaEntity)
      def poDetails = purchaseOrderDetailTestDataLoaderService.stream(20, company, purchaseOrderIn, shipToIn, vendorIn)
         .map { new PurchaseOrderDetailDTO(it)}.sorted { o1, o2 -> o1.id <=> o2.id }.toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(4, 5, "id", "ASC")
      def pageFive = new StandardPageRequest(5, 5, "id", "ASC")
      def firstPage = poDetails[0..4]
      def secondPage = poDetails[5..9]
      def lastPage = poDetails[15..19]

      when:
      def result = get("$path${pageOne}")

      then:
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageOne
         totalElements == 20
         totalPages == 4
         first == true
         last == false
         elements.size() == 5
         elements.eachWithIndex { pageOneResult, index ->
            with(pageOneResult) {
               id == firstPage[index].id
               number == firstPage[index].number
               purchaseOrder.id == firstPage[index].purchaseOrder.id
               itemfileNumber == firstPage[index].itemfileNumber
               orderQuantity == firstPage[index].orderQuantity
               receivedQuantity == firstPage[index].receivedQuantity
               cost == firstPage[index].cost
               message == firstPage[index].message
               color == firstPage[index].color
               fabric == firstPage[index].fabric
               cancelledQuantity == firstPage[index].cancelledQuantity
               cancelledTempQuantity == firstPage[index].cancelledTempQuantity
               shipTo.id == firstPage[index].shipTo.myId()
               requiredDate == firstPage[index].requiredDate.toString()
               dateOrdered == firstPage[index].dateOrdered.toString()
               freightPerItem == firstPage[index].freightPerItem
               tempQuantityToReceive == firstPage[index].tempQuantityToReceive
               vendor.id == firstPage[index].vendor.id
               lastReceivedDate == firstPage[index].lastReceivedDate.toString()
               landedCost == firstPage[index].landedCost

               with(statusType) {
                  value == firstPage[index].statusType.value
                  description == firstPage[index].statusType.description
               }

               with(purchaseOrderRequisitionIndicatorType) {
                  value == firstPage[index].purchaseOrderRequisitionIndicatorType.value
                  description == firstPage[index].purchaseOrderRequisitionIndicatorType.description
               }

               with(exceptionIndicatorType) {
                  value == firstPage[index].exceptionIndicatorType.value
                  description == firstPage[index].exceptionIndicatorType.description
               }

               convertedPurchaseOrderNumber == firstPage[index].convertedPurchaseOrderNumber
               approvedIndicator == firstPage[index].approvedIndicator
            }
         }
      }

      when:
      result = get("$path${pageTwo}")

      then:
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageTwo
         totalElements == 20
         totalPages == 4
         first == false
         last == false
         elements.size() == 5
         elements.eachWithIndex { pageTwoResult, index ->
            with(pageTwoResult) {
               id == secondPage[index].id
               number == secondPage[index].number
               purchaseOrder.id == secondPage[index].purchaseOrder.id
               itemfileNumber == secondPage[index].itemfileNumber
               orderQuantity == secondPage[index].orderQuantity
               receivedQuantity == secondPage[index].receivedQuantity
               cost == secondPage[index].cost
               message == secondPage[index].message
               color == secondPage[index].color
               fabric == secondPage[index].fabric
               cancelledQuantity == secondPage[index].cancelledQuantity
               cancelledTempQuantity == secondPage[index].cancelledTempQuantity
               shipTo.id == secondPage[index].shipTo.myId()
               requiredDate == secondPage[index].requiredDate.toString()
               dateOrdered == secondPage[index].dateOrdered.toString()
               freightPerItem == secondPage[index].freightPerItem
               tempQuantityToReceive == secondPage[index].tempQuantityToReceive
               vendor.id == secondPage[index].vendor.id
               lastReceivedDate == secondPage[index].lastReceivedDate.toString()
               landedCost == secondPage[index].landedCost

               with(statusType) {
                  value == secondPage[index].statusType.value
                  description == secondPage[index].statusType.description
               }

               with(purchaseOrderRequisitionIndicatorType) {
                  value == secondPage[index].purchaseOrderRequisitionIndicatorType.value
                  description == secondPage[index].purchaseOrderRequisitionIndicatorType.description
               }

               with(exceptionIndicatorType) {
                  value == secondPage[index].exceptionIndicatorType.value
                  description == secondPage[index].exceptionIndicatorType.description
               }

               convertedPurchaseOrderNumber == secondPage[index].convertedPurchaseOrderNumber
               approvedIndicator == secondPage[index].approvedIndicator
            }
         }
      }

      when:
      result = get("$path${pageLast}")

      then:
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageLast
         totalElements == 20
         totalPages == 4
         first == false
         last == true
         elements.size() == 5
         elements.eachWithIndex { pageLastResult, index ->
            with(pageLastResult) {
               id == lastPage[index].id
               number == lastPage[index].number
               purchaseOrder.id == lastPage[index].purchaseOrder.id
               itemfileNumber == lastPage[index].itemfileNumber
               orderQuantity == lastPage[index].orderQuantity
               receivedQuantity == lastPage[index].receivedQuantity
               cost == lastPage[index].cost
               message == lastPage[index].message
               color == lastPage[index].color
               fabric == lastPage[index].fabric
               cancelledQuantity == lastPage[index].cancelledQuantity
               cancelledTempQuantity == lastPage[index].cancelledTempQuantity
               shipTo.id == lastPage[index].shipTo.myId()
               requiredDate == lastPage[index].requiredDate.toString()
               dateOrdered == lastPage[index].dateOrdered.toString()
               freightPerItem == lastPage[index].freightPerItem
               tempQuantityToReceive == lastPage[index].tempQuantityToReceive
               vendor.id == lastPage[index].vendor.id
               lastReceivedDate == lastPage[index].lastReceivedDate.toString()
               landedCost == lastPage[index].landedCost

               with(statusType) {
                  value == lastPage[index].statusType.value
                  description == lastPage[index].statusType.description
               }

               with(purchaseOrderRequisitionIndicatorType) {
                  value == lastPage[index].purchaseOrderRequisitionIndicatorType.value
                  description == lastPage[index].purchaseOrderRequisitionIndicatorType.description
               }

               with(exceptionIndicatorType) {
                  value == lastPage[index].exceptionIndicatorType.value
                  description == lastPage[index].exceptionIndicatorType.description
               }

               convertedPurchaseOrderNumber == lastPage[index].convertedPurchaseOrderNumber
               approvedIndicator == lastPage[index].approvedIndicator
            }
         }
      }

      when:
      get("$path/${pageFive}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create valid purchase order detail" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final poDetailDTO = purchaseOrderDetailTestDataLoaderService.singleDTO(new PurchaseOrderDTO(purchaseOrderIn), new SimpleLegacyIdentifiableDTO(shipToIn.myId()), new SimpleIdentifiableDTO(vendorIn))

      when:
      def result = post("$path/", poDetailDTO)

      then:
      notThrown(HttpClientResponseException)
      result != null
      result.id != null
      with(result) {
         id != null
         number == poDetailDTO.number
         purchaseOrder.id == poDetailDTO.purchaseOrder.id
         itemfileNumber == poDetailDTO.itemfileNumber
         orderQuantity == poDetailDTO.orderQuantity
         receivedQuantity == poDetailDTO.receivedQuantity
         cost == poDetailDTO.cost
         message == poDetailDTO.message
         color == poDetailDTO.color
         fabric == poDetailDTO.fabric
         cancelledQuantity == poDetailDTO.cancelledQuantity
         cancelledTempQuantity == poDetailDTO.cancelledTempQuantity
         shipTo.id == poDetailDTO.shipTo.myId()
         requiredDate == poDetailDTO.requiredDate.toString()
         dateOrdered == poDetailDTO.dateOrdered.toString()
         freightPerItem == poDetailDTO.freightPerItem
         tempQuantityToReceive == poDetailDTO.tempQuantityToReceive
         vendor.id == poDetailDTO.vendor.id
         lastReceivedDate == poDetailDTO.lastReceivedDate.toString()
         landedCost == poDetailDTO.landedCost

         with(statusType) {
            value == poDetailDTO.statusType.value
            description == poDetailDTO.statusType.description
         }

         with(purchaseOrderRequisitionIndicatorType) {
            value == poDetailDTO.purchaseOrderRequisitionIndicatorType.value
            description == poDetailDTO.purchaseOrderRequisitionIndicatorType.description
         }

         with(exceptionIndicatorType) {
            value == poDetailDTO.exceptionIndicatorType.value
            description == poDetailDTO.exceptionIndicatorType.description
         }

         convertedPurchaseOrderNumber == poDetailDTO.convertedPurchaseOrderNumber
         approvedIndicator == poDetailDTO.approvedIndicator
      }
   }

   void "create valid purchase order detail without nullable properties" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final poDetailDTO = purchaseOrderDetailTestDataLoaderService.singleDTO(new PurchaseOrderDTO(purchaseOrderIn), new SimpleLegacyIdentifiableDTO(shipToIn.myId()), new SimpleIdentifiableDTO(vendorIn))
      poDetailDTO.message = null
      poDetailDTO.color = null
      poDetailDTO.fabric = null
      poDetailDTO.cancelledQuantity = null
      poDetailDTO.cancelledTempQuantity = null
      poDetailDTO.requiredDate = null
      poDetailDTO.dateOrdered = null
      poDetailDTO.freightPerItem = null
      poDetailDTO.tempQuantityToReceive = null
      poDetailDTO.lastReceivedDate = null
      poDetailDTO.landedCost = null

      when:
      def result = post("$path", poDetailDTO)

      then:
      notThrown(HttpClientResponseException)
      result != null
      result.id != null
      with(result) {
         id != null
         number == poDetailDTO.number
         purchaseOrder.id == poDetailDTO.purchaseOrder.id
         itemfileNumber == poDetailDTO.itemfileNumber
         orderQuantity == poDetailDTO.orderQuantity
         receivedQuantity == poDetailDTO.receivedQuantity
         cost == poDetailDTO.cost
         message == poDetailDTO.message
         color == poDetailDTO.color
         fabric == poDetailDTO.fabric
         cancelledQuantity == poDetailDTO.cancelledQuantity
         cancelledTempQuantity == poDetailDTO.cancelledTempQuantity
         shipTo.id == poDetailDTO.shipTo.myId()
         requiredDate == null
         dateOrdered == null
         freightPerItem == poDetailDTO.freightPerItem
         tempQuantityToReceive == poDetailDTO.tempQuantityToReceive
         vendor.id == poDetailDTO.vendor.id
         lastReceivedDate == null
         landedCost == poDetailDTO.landedCost

         with(statusType) {
            value == poDetailDTO.statusType.value
            description == poDetailDTO.statusType.description
         }

         with(purchaseOrderRequisitionIndicatorType) {
            value == poDetailDTO.purchaseOrderRequisitionIndicatorType.value
            description == poDetailDTO.purchaseOrderRequisitionIndicatorType.description
         }

         with(exceptionIndicatorType) {
            value == poDetailDTO.exceptionIndicatorType.value
            description == poDetailDTO.exceptionIndicatorType.description
         }

         convertedPurchaseOrderNumber == poDetailDTO.convertedPurchaseOrderNumber
         approvedIndicator == poDetailDTO.approvedIndicator
      }
   }

   @Unroll
   void "create invalid purchase order detail without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final poDetailDTO = purchaseOrderDetailTestDataLoaderService.singleDTO(new PurchaseOrderDTO(purchaseOrderIn), new SimpleLegacyIdentifiableDTO(shipToIn.myId()), new SimpleIdentifiableDTO(vendorIn))
      poDetailDTO["$nonNullableProp"] = null

      when:
      post("$path/", poDetailDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                         || errorResponsePath
      'approvedIndicator'                     || 'approvedIndicator'
      'convertedPurchaseOrderNumber'          || 'convertedPurchaseOrderNumber'
      'cost'                                  || 'cost'
      'exceptionIndicatorType'                || 'exceptionIndicatorType'
      'itemfileNumber'                        || 'itemfileNumber'
      'purchaseOrder'                         || 'purchaseOrder'
      'purchaseOrderRequisitionIndicatorType' || 'purchaseOrderRequisitionIndicatorType'
      'orderQuantity'                         || 'orderQuantity'
      'receivedQuantity'                      || 'receivedQuantity'
      'shipTo'                                || 'shipTo'
      'statusType'                            || 'statusType'
      'vendor'                                || 'vendor'
   }

   void "update valid purchase order detail" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final existingPODetail = purchaseOrderDetailTestDataLoaderService.single(company, purchaseOrderIn, shipToIn, vendorIn)
      final updatedPODetailDTO = purchaseOrderDetailTestDataLoaderService.singleDTO(new PurchaseOrderDTO(purchaseOrderIn), new SimpleLegacyIdentifiableDTO(shipToIn.myId()), new SimpleIdentifiableDTO(vendorIn))
      updatedPODetailDTO.id = existingPODetail.id

      when:
      def result = put("$path/${existingPODetail.id}", updatedPODetailDTO)

      then:
      notThrown(HttpClientResponseException)
      result != null
      with(result) {
         id == updatedPODetailDTO.id
         number == updatedPODetailDTO.number
         purchaseOrder.id == updatedPODetailDTO.purchaseOrder.id
         itemfileNumber == updatedPODetailDTO.itemfileNumber
         orderQuantity == updatedPODetailDTO.orderQuantity
         receivedQuantity == updatedPODetailDTO.receivedQuantity
         cost == updatedPODetailDTO.cost
         message == updatedPODetailDTO.message
         color == updatedPODetailDTO.color
         fabric == updatedPODetailDTO.fabric
         cancelledQuantity == updatedPODetailDTO.cancelledQuantity
         cancelledTempQuantity == updatedPODetailDTO.cancelledTempQuantity
         shipTo.id == updatedPODetailDTO.shipTo.myId()
         requiredDate == updatedPODetailDTO.requiredDate.toString()
         dateOrdered == updatedPODetailDTO.dateOrdered.toString()
         freightPerItem == updatedPODetailDTO.freightPerItem
         tempQuantityToReceive == updatedPODetailDTO.tempQuantityToReceive
         vendor.id == updatedPODetailDTO.vendor.id
         lastReceivedDate == updatedPODetailDTO.lastReceivedDate.toString()
         landedCost == updatedPODetailDTO.landedCost

         with(statusType) {
            value == updatedPODetailDTO.statusType.value
            description == updatedPODetailDTO.statusType.description
         }

         with(purchaseOrderRequisitionIndicatorType) {
            value == updatedPODetailDTO.purchaseOrderRequisitionIndicatorType.value
            description == updatedPODetailDTO.purchaseOrderRequisitionIndicatorType.description
         }

         with(exceptionIndicatorType) {
            value == updatedPODetailDTO.exceptionIndicatorType.value
            description == updatedPODetailDTO.exceptionIndicatorType.description
         }

         convertedPurchaseOrderNumber == updatedPODetailDTO.convertedPurchaseOrderNumber
         approvedIndicator == updatedPODetailDTO.approvedIndicator
      }
   }

   void "update valid purchase order detail without nullable properties" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final existingPODetail = purchaseOrderDetailTestDataLoaderService.single(company, purchaseOrderIn, shipToIn, vendorIn)
      final updatedPODetailDTO = purchaseOrderDetailTestDataLoaderService.singleDTO(new PurchaseOrderDTO(purchaseOrderIn), new SimpleLegacyIdentifiableDTO(shipToIn.myId()), new SimpleIdentifiableDTO(vendorIn))
      updatedPODetailDTO.id = existingPODetail.id
      updatedPODetailDTO.message = null
      updatedPODetailDTO.color = null
      updatedPODetailDTO.fabric = null
      updatedPODetailDTO.cancelledQuantity = null
      updatedPODetailDTO.cancelledTempQuantity = null
      updatedPODetailDTO.requiredDate = null
      updatedPODetailDTO.dateOrdered = null
      updatedPODetailDTO.freightPerItem = null
      updatedPODetailDTO.tempQuantityToReceive = null
      updatedPODetailDTO.lastReceivedDate = null
      updatedPODetailDTO.landedCost = null


      when:
      def result = put("$path/${existingPODetail.id}", updatedPODetailDTO)

      then:
      notThrown(HttpClientResponseException)
      result != null
      with(result) {
         id == updatedPODetailDTO.id
         number == updatedPODetailDTO.number
         purchaseOrder.id == updatedPODetailDTO.purchaseOrder.id
         itemfileNumber == updatedPODetailDTO.itemfileNumber
         orderQuantity == updatedPODetailDTO.orderQuantity
         receivedQuantity == updatedPODetailDTO.receivedQuantity
         cost == updatedPODetailDTO.cost
         message == updatedPODetailDTO.message
         color == updatedPODetailDTO.color
         fabric == updatedPODetailDTO.fabric
         cancelledQuantity == updatedPODetailDTO.cancelledQuantity
         cancelledTempQuantity == updatedPODetailDTO.cancelledTempQuantity
         shipTo.id == updatedPODetailDTO.shipTo.myId()
         requiredDate == null
         dateOrdered == null
         freightPerItem == updatedPODetailDTO.freightPerItem
         tempQuantityToReceive == updatedPODetailDTO.tempQuantityToReceive
         vendor.id == updatedPODetailDTO.vendor.id
         lastReceivedDate == null
         landedCost == updatedPODetailDTO.landedCost

         with(statusType) {
            value == updatedPODetailDTO.statusType.value
            description == updatedPODetailDTO.statusType.description
         }

         with(purchaseOrderRequisitionIndicatorType) {
            value == updatedPODetailDTO.purchaseOrderRequisitionIndicatorType.value
            description == updatedPODetailDTO.purchaseOrderRequisitionIndicatorType.description
         }

         with(exceptionIndicatorType) {
            value == updatedPODetailDTO.exceptionIndicatorType.value
            description == updatedPODetailDTO.exceptionIndicatorType.description
         }

         convertedPurchaseOrderNumber == updatedPODetailDTO.convertedPurchaseOrderNumber
         approvedIndicator == updatedPODetailDTO.approvedIndicator
      }
   }

   @Unroll
   void "update invalid purchase order detail without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final existingPODetail = purchaseOrderDetailTestDataLoaderService.single(company, purchaseOrderIn, shipToIn, vendorIn)
      final updatedPODetailDTO = purchaseOrderDetailTestDataLoaderService.singleDTO(new PurchaseOrderDTO(purchaseOrderIn), new SimpleLegacyIdentifiableDTO(shipToIn.myId()), new SimpleIdentifiableDTO(vendorIn))
      updatedPODetailDTO.id = existingPODetail.id
      updatedPODetailDTO["$nonNullableProp"] = null

      when:
      put("$path/${existingPODetail.id}", updatedPODetailDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                         || errorResponsePath
      'approvedIndicator'                     || 'approvedIndicator'
      'convertedPurchaseOrderNumber'          || 'convertedPurchaseOrderNumber'
      'cost'                                  || 'cost'
      'exceptionIndicatorType'                || 'exceptionIndicatorType'
      'itemfileNumber'                        || 'itemfileNumber'
      'purchaseOrder'                         || 'purchaseOrder'
      'purchaseOrderRequisitionIndicatorType' || 'purchaseOrderRequisitionIndicatorType'
      'orderQuantity'                         || 'orderQuantity'
      'receivedQuantity'                      || 'receivedQuantity'
      'shipTo'                                || 'shipTo'
      'statusType'                            || 'statusType'
      'vendor'                                || 'vendor'
   }

   void "delete purchase order detail" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final poDetail = purchaseOrderDetailTestDataLoaderService.single(company, purchaseOrderIn, shipToIn, vendorIn)

      when:
      delete("$path/${poDetail.id}", )

      then: "purchase order detail for user's company is deleted"
      notThrown(HttpClientResponseException)

      when:
      get("$path/${poDetail.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${poDetail.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "recreate deleted purchase order detail" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaTestDataLoaderService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final poVendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final purchaseOrderIn = purchaseOrderTestDataLoaderService.single(
         company,
         poVendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn as ShipViaEntity,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      )
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final poDetailDTO = purchaseOrderDetailTestDataLoaderService.singleDTO(new PurchaseOrderDTO(purchaseOrderIn), new SimpleLegacyIdentifiableDTO(shipToIn.myId()), new SimpleIdentifiableDTO(vendorIn))

      when: // create a purchase order detail
      def response1 = post("$path/", poDetailDTO)

      then:
      notThrown(HttpClientResponseException)
      response1 != null
      response1.id != null
      with(response1) {
         id != null
         number == poDetailDTO.number
         purchaseOrder.id == poDetailDTO.purchaseOrder.id
         itemfileNumber == poDetailDTO.itemfileNumber
         orderQuantity == poDetailDTO.orderQuantity
         receivedQuantity == poDetailDTO.receivedQuantity
         cost == poDetailDTO.cost
         message == poDetailDTO.message
         color == poDetailDTO.color
         fabric == poDetailDTO.fabric
         cancelledQuantity == poDetailDTO.cancelledQuantity
         cancelledTempQuantity == poDetailDTO.cancelledTempQuantity
         shipTo.id == poDetailDTO.shipTo.myId()
         requiredDate == poDetailDTO.requiredDate.toString()
         dateOrdered == poDetailDTO.dateOrdered.toString()
         freightPerItem == poDetailDTO.freightPerItem
         tempQuantityToReceive == poDetailDTO.tempQuantityToReceive
         vendor.id == poDetailDTO.vendor.id
         lastReceivedDate == poDetailDTO.lastReceivedDate.toString()
         landedCost == poDetailDTO.landedCost

         with(statusType) {
            value == poDetailDTO.statusType.value
            description == poDetailDTO.statusType.description
         }

         with(purchaseOrderRequisitionIndicatorType) {
            value == poDetailDTO.purchaseOrderRequisitionIndicatorType.value
            description == poDetailDTO.purchaseOrderRequisitionIndicatorType.description
         }

         with(exceptionIndicatorType) {
            value == poDetailDTO.exceptionIndicatorType.value
            description == poDetailDTO.exceptionIndicatorType.description
         }

         convertedPurchaseOrderNumber == poDetailDTO.convertedPurchaseOrderNumber
         approvedIndicator == poDetailDTO.approvedIndicator
      }

      when: // delete purchase order detail
      delete("$path/$response1.id")

      then: "purchase order detail of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate purchase order detail
      def response2 = post("$path/", poDetailDTO)

      then:
      notThrown(HttpClientResponseException)
      response2 != null
      response2.id != null
      with(response2) {
         id != null
         number == poDetailDTO.number
         purchaseOrder.id == poDetailDTO.purchaseOrder.id
         itemfileNumber == poDetailDTO.itemfileNumber
         orderQuantity == poDetailDTO.orderQuantity
         receivedQuantity == poDetailDTO.receivedQuantity
         cost == poDetailDTO.cost
         message == poDetailDTO.message
         color == poDetailDTO.color
         fabric == poDetailDTO.fabric
         cancelledQuantity == poDetailDTO.cancelledQuantity
         cancelledTempQuantity == poDetailDTO.cancelledTempQuantity
         shipTo.id == poDetailDTO.shipTo.myId()
         requiredDate == poDetailDTO.requiredDate.toString()
         dateOrdered == poDetailDTO.dateOrdered.toString()
         freightPerItem == poDetailDTO.freightPerItem
         tempQuantityToReceive == poDetailDTO.tempQuantityToReceive
         vendor.id == poDetailDTO.vendor.id
         lastReceivedDate == poDetailDTO.lastReceivedDate.toString()
         landedCost == poDetailDTO.landedCost

         with(statusType) {
            value == poDetailDTO.statusType.value
            description == poDetailDTO.statusType.description
         }

         with(purchaseOrderRequisitionIndicatorType) {
            value == poDetailDTO.purchaseOrderRequisitionIndicatorType.value
            description == poDetailDTO.purchaseOrderRequisitionIndicatorType.description
         }

         with(exceptionIndicatorType) {
            value == poDetailDTO.exceptionIndicatorType.value
            description == poDetailDTO.exceptionIndicatorType.description
         }

         convertedPurchaseOrderNumber == poDetailDTO.convertedPurchaseOrderNumber
         approvedIndicator == poDetailDTO.approvedIndicator
      }

      when: // delete purchase order detail again
      delete("$path/$response2.id")

      then: "purchase order detail of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
