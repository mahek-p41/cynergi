package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject
import java.time.OffsetDateTime
import java.time.ZoneOffset

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class PurchaseOrderControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/purchase-order"

   @Inject AccountDataLoaderService accountDataLoaderService
   @Inject PurchaseOrderDataLoaderService purchaseOrderDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService

   void "fetch one" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      final def purchaseOrder = purchaseOrderDataLoaderService.single(
         company,
         vendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn,
         customerAccountIn
      )

      when:
      def result = get("$path/${purchaseOrder.id}")

      then:
      notThrown(HttpClientResponseException)
      with(result) {
         id == purchaseOrder.id
         number == purchaseOrder.number
         vendor.id == purchaseOrder.vendor.id

         with(statusType) {
            value == purchaseOrder.statusType.value
            description == purchaseOrder.statusType.description
         }

         orderDate == purchaseOrder.orderDate.toString()

         with(type) {
            value == purchaseOrder.type.value
            description == purchaseOrder.type.description
         }

         with(freightOnboardType) {
            value == purchaseOrder.freightOnboardType.value
            description == purchaseOrder.freightOnboardType.description
         }

         with(freightTermType) {
            value == purchaseOrder.freightTermType.value
            description == purchaseOrder.freightTermType.description
         }

         with(shipLocationType) {
            value == purchaseOrder.shipLocationType.value
            description == purchaseOrder.shipLocationType.description
         }

         approvedBy.number == purchaseOrder.approvedBy.number
         totalAmount == purchaseOrder.totalAmount
         receivedAmount == purchaseOrder.receivedAmount
         paidAmount == purchaseOrder.paidAmount
         purchaseAgent.number == purchaseOrder.purchaseAgent.number
         shipVia.id == purchaseOrder.shipVia.id
         requiredDate == purchaseOrder.requiredDate.toString()
         shipTo.id == purchaseOrder.shipTo.myId()
         paymentTermType.id == purchaseOrder.paymentTermType.id
         message == purchaseOrder.message
         totalLandedAmount == purchaseOrder.totalLandedAmount
         totalFreightAmount == purchaseOrder.totalFreightAmount

         with(exceptionIndicatorType) {
            value == purchaseOrder.exceptionIndicatorType.value
            description == purchaseOrder.exceptionIndicatorType.description
         }

         vendorSubmittedTime.with { OffsetDateTime.parse(it) } == purchaseOrder.vendorSubmittedTime.withOffsetSameInstant(ZoneOffset.UTC)
         vendorSubmittedEmployee.number == purchaseOrder.vendorSubmittedEmployee.number
         ecommerceIndicator == purchaseOrder.ecommerceIndicator
         customerAccount.id == purchaseOrder.customerAccount.id
      }
   }

   void "fetch one not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size()== 1
      response.message == "0 was unable to be found"
   }

   void "fetch all" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      def purchaseOrders = purchaseOrderDataLoaderService.stream(
         20,
         company,
         vendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn,
         customerAccountIn
      ).map { new PurchaseOrderDTO(it)}.sorted { o1, o2 -> o1.id <=> o2.id }.toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(4, 5, "id", "ASC")
      def pageFive = new StandardPageRequest(5, 5, "id", "ASC")
      def firstPage = purchaseOrders[0..4]
      def secondPage = purchaseOrders[5..9]
      def lastPage = purchaseOrders[15..19]

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
               vendor.id == firstPage[index].vendor.id

               with(statusType) {
                  value == firstPage[index].statusType.value
                  description == firstPage[index].statusType.description
               }

               orderDate == firstPage[index].orderDate.toString()

               with(type) {
                  value == firstPage[index].type.value
                  description == firstPage[index].type.description
               }

               with(freightOnboardType) {
                  value == firstPage[index].freightOnboardType.value
                  description == firstPage[index].freightOnboardType.description
               }

               with(freightTermType) {
                  value == firstPage[index].freightTermType.value
                  description == firstPage[index].freightTermType.description
               }

               with(shipLocationType) {
                  value == firstPage[index].shipLocationType.value
                  description == firstPage[index].shipLocationType.description
               }

               approvedBy.number == firstPage[index].approvedBy.number
               totalAmount == firstPage[index].totalAmount
               receivedAmount == firstPage[index].receivedAmount
               paidAmount == firstPage[index].paidAmount
               purchaseAgent.number == firstPage[index].purchaseAgent.number
               shipVia.id == firstPage[index].shipVia.id
               requiredDate == firstPage[index].requiredDate.toString()
               shipTo.id == firstPage[index].shipTo.myId()
               paymentTermType.id == firstPage[index].paymentTermType.id
               message == firstPage[index].message
               totalLandedAmount == firstPage[index].totalLandedAmount
               totalFreightAmount == firstPage[index].totalFreightAmount

               with(exceptionIndicatorType) {
                  value == firstPage[index].exceptionIndicatorType.value
                  description == firstPage[index].exceptionIndicatorType.description
               }

               vendorSubmittedTime.with { OffsetDateTime.parse(it) } == firstPage[index].vendorSubmittedTime.withOffsetSameInstant(ZoneOffset.UTC)
               vendorSubmittedEmployee.number == firstPage[index].vendorSubmittedEmployee.number
               ecommerceIndicator == firstPage[index].ecommerceIndicator
               customerAccount.id == firstPage[index].customerAccount.id
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
               vendor.id == secondPage[index].vendor.id

               with(statusType) {
                  value == secondPage[index].statusType.value
                  description == secondPage[index].statusType.description
               }

               orderDate == secondPage[index].orderDate.toString()

               with(type) {
                  value == secondPage[index].type.value
                  description == secondPage[index].type.description
               }

               with(freightOnboardType) {
                  value == secondPage[index].freightOnboardType.value
                  description == secondPage[index].freightOnboardType.description
               }

               with(freightTermType) {
                  value == secondPage[index].freightTermType.value
                  description == secondPage[index].freightTermType.description
               }

               with(shipLocationType) {
                  value == secondPage[index].shipLocationType.value
                  description == secondPage[index].shipLocationType.description
               }

               approvedBy.number == secondPage[index].approvedBy.number
               totalAmount == secondPage[index].totalAmount
               receivedAmount == secondPage[index].receivedAmount
               paidAmount == secondPage[index].paidAmount
               purchaseAgent.number == secondPage[index].purchaseAgent.number
               shipVia.id == secondPage[index].shipVia.id
               requiredDate == secondPage[index].requiredDate.toString()
               shipTo.id == secondPage[index].shipTo.myId()
               paymentTermType.id == secondPage[index].paymentTermType.id
               message == secondPage[index].message
               totalLandedAmount == secondPage[index].totalLandedAmount
               totalFreightAmount == secondPage[index].totalFreightAmount

               with(exceptionIndicatorType) {
                  value == secondPage[index].exceptionIndicatorType.value
                  description == secondPage[index].exceptionIndicatorType.description
               }

               vendorSubmittedTime.with { OffsetDateTime.parse(it) } == secondPage[index].vendorSubmittedTime.withOffsetSameInstant(ZoneOffset.UTC)
               vendorSubmittedEmployee.number == secondPage[index].vendorSubmittedEmployee.number
               ecommerceIndicator == secondPage[index].ecommerceIndicator
               customerAccount.id == secondPage[index].customerAccount.id
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
               vendor.id == lastPage[index].vendor.id

               with(statusType) {
                  value == lastPage[index].statusType.value
                  description == lastPage[index].statusType.description
               }

               orderDate == lastPage[index].orderDate.toString()

               with(type) {
                  value == lastPage[index].type.value
                  description == lastPage[index].type.description
               }

               with(freightOnboardType) {
                  value == lastPage[index].freightOnboardType.value
                  description == lastPage[index].freightOnboardType.description
               }

               with(freightTermType) {
                  value == lastPage[index].freightTermType.value
                  description == lastPage[index].freightTermType.description
               }

               with(shipLocationType) {
                  value == lastPage[index].shipLocationType.value
                  description == lastPage[index].shipLocationType.description
               }

               approvedBy.number == lastPage[index].approvedBy.number
               totalAmount == lastPage[index].totalAmount
               receivedAmount == lastPage[index].receivedAmount
               paidAmount == lastPage[index].paidAmount
               purchaseAgent.number == lastPage[index].purchaseAgent.number
               shipVia.id == lastPage[index].shipVia.id
               requiredDate == lastPage[index].requiredDate.toString()
               shipTo.id == lastPage[index].shipTo.myId()
               paymentTermType.id == lastPage[index].paymentTermType.id
               message == lastPage[index].message
               totalLandedAmount == lastPage[index].totalLandedAmount
               totalFreightAmount == lastPage[index].totalFreightAmount

               with(exceptionIndicatorType) {
                  value == lastPage[index].exceptionIndicatorType.value
                  description == lastPage[index].exceptionIndicatorType.description
               }

               vendorSubmittedTime.with { OffsetDateTime.parse(it) } == lastPage[index].vendorSubmittedTime.withOffsetSameInstant(ZoneOffset.UTC)
               vendorSubmittedEmployee.number == lastPage[index].vendorSubmittedEmployee.number
               ecommerceIndicator == lastPage[index].ecommerceIndicator
               customerAccount.id == lastPage[index].customerAccount.id
            }
         }
      }

      when:
      get("$path/${pageFive}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create valid purchase order" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      final def purchaseOrder = purchaseOrderDataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(vendorIn),
         approvedByIn,
         purchaseAgentIn,
         new SimpleIdentifiableDTO(shipViaIn),
         new SimpleIdentifiableDTO(shipToIn.myId()),
         new SimpleIdentifiableDTO(paymentTermTypeIn),
         vendorSubmittedEmployeeIn,
         new SimpleIdentifiableDTO(customerAccountIn)
      )

      when:
      def result = post("$path/", purchaseOrder)

      then:
      notThrown(HttpClientResponseException)
      result != null
      result.id != null
      with(result) {
         id > 0
         number == purchaseOrder.number
         vendor.id == purchaseOrder.vendor.id

         with(statusType) {
            value == purchaseOrder.statusType.value
            description == purchaseOrder.statusType.description
         }

         orderDate == purchaseOrder.orderDate.toString()

         with(type) {
            value == purchaseOrder.type.value
            description == purchaseOrder.type.description
         }

         with(freightOnboardType) {
            value == purchaseOrder.freightOnboardType.value
            description == purchaseOrder.freightOnboardType.description
         }

         with(freightTermType) {
            value == purchaseOrder.freightTermType.value
            description == purchaseOrder.freightTermType.description
         }

         with(shipLocationType) {
            value == purchaseOrder.shipLocationType.value
            description == purchaseOrder.shipLocationType.description
         }

         approvedBy.number == purchaseOrder.approvedBy.number
         totalAmount == purchaseOrder.totalAmount
         receivedAmount == purchaseOrder.receivedAmount
         paidAmount == purchaseOrder.paidAmount
         purchaseAgent.number == purchaseOrder.purchaseAgent.number
         shipVia.id == purchaseOrder.shipVia.id
         requiredDate == purchaseOrder.requiredDate.toString()
         shipTo.id == purchaseOrder.shipTo.myId()
         paymentTermType.id == purchaseOrder.paymentTermType.id
         message == purchaseOrder.message
         totalLandedAmount == purchaseOrder.totalLandedAmount
         totalFreightAmount == purchaseOrder.totalFreightAmount

         with(exceptionIndicatorType) {
            value == purchaseOrder.exceptionIndicatorType.value
            description == purchaseOrder.exceptionIndicatorType.description
         }

         vendorSubmittedTime.with { OffsetDateTime.parse(it) } == purchaseOrder.vendorSubmittedTime.withOffsetSameInstant(ZoneOffset.UTC)
         vendorSubmittedEmployee.number == purchaseOrder.vendorSubmittedEmployee.number
         ecommerceIndicator == purchaseOrder.ecommerceIndicator
         customerAccount.id == purchaseOrder.customerAccount.id
      }
   }

   void "create valid purchase order with nullable properties" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      final def purchaseOrderDTO = purchaseOrderDataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(vendorIn),
         approvedByIn,
         purchaseAgentIn,
         new SimpleIdentifiableDTO(shipViaIn),
         new SimpleIdentifiableDTO(shipToIn.myId()),
         new SimpleIdentifiableDTO(paymentTermTypeIn),
         vendorSubmittedEmployeeIn,
         new SimpleIdentifiableDTO(customerAccountIn)
      )
      purchaseOrderDTO.totalAmount = null
      purchaseOrderDTO.receivedAmount = null
      purchaseOrderDTO.paidAmount = null
      purchaseOrderDTO.message = null
      purchaseOrderDTO.totalLandedAmount = null
      purchaseOrderDTO.totalFreightAmount = null
      purchaseOrderDTO.vendorSubmittedTime = null
      purchaseOrderDTO.vendorSubmittedEmployee = null
      purchaseOrderDTO.customerAccount = null

      when:
      def result = post("$path", purchaseOrderDTO)

      then:
      notThrown(HttpClientResponseException)
      result != null
      result.id != null
      with(result) {
         id > 0
         number == purchaseOrderDTO.number
         vendor.id == purchaseOrderDTO.vendor.id

         with(statusType) {
            value == purchaseOrderDTO.statusType.value
            description == purchaseOrderDTO.statusType.description
         }

         orderDate == purchaseOrderDTO.orderDate.toString()

         with(type) {
            value == purchaseOrderDTO.type.value
            description == purchaseOrderDTO.type.description
         }

         with(freightOnboardType) {
            value == purchaseOrderDTO.freightOnboardType.value
            description == purchaseOrderDTO.freightOnboardType.description
         }

         with(freightTermType) {
            value == purchaseOrderDTO.freightTermType.value
            description == purchaseOrderDTO.freightTermType.description
         }

         with(shipLocationType) {
            value == purchaseOrderDTO.shipLocationType.value
            description == purchaseOrderDTO.shipLocationType.description
         }

         approvedBy.number == purchaseOrderDTO.approvedBy.number
         totalAmount == purchaseOrderDTO.totalAmount
         receivedAmount == purchaseOrderDTO.receivedAmount
         paidAmount == purchaseOrderDTO.paidAmount
         purchaseAgent.number == purchaseOrderDTO.purchaseAgent.number
         shipVia.id == purchaseOrderDTO.shipVia.id
         requiredDate == purchaseOrderDTO.requiredDate.toString()
         shipTo.id == purchaseOrderDTO.shipTo.myId()
         paymentTermType.id == purchaseOrderDTO.paymentTermType.id
         message == purchaseOrderDTO.message
         totalLandedAmount == purchaseOrderDTO.totalLandedAmount
         totalFreightAmount == purchaseOrderDTO.totalFreightAmount

         with(exceptionIndicatorType) {
            value == purchaseOrderDTO.exceptionIndicatorType.value
            description == purchaseOrderDTO.exceptionIndicatorType.description
         }

         vendorSubmittedTime == purchaseOrderDTO.vendorSubmittedTime
         vendorSubmittedEmployee == purchaseOrderDTO.vendorSubmittedEmployee
         ecommerceIndicator == purchaseOrderDTO.ecommerceIndicator
         customerAccount == purchaseOrderDTO.customerAccount
      }
   }

   @Unroll
   void "create invalid purchase order without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      final def purchaseOrderDTO = purchaseOrderDataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(vendorIn),
         approvedByIn,
         purchaseAgentIn,
         new SimpleIdentifiableDTO(shipViaIn),
         new SimpleIdentifiableDTO(shipToIn.myId()),
         new SimpleIdentifiableDTO(paymentTermTypeIn),
         vendorSubmittedEmployeeIn,
         new SimpleIdentifiableDTO(customerAccountIn)
      )
      purchaseOrderDTO["$nonNullableProp"] = null

      when:
      post("$path/", purchaseOrderDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                     || errorResponsePath
      'approvedBy'                        || 'approvedBy'
      'ecommerceIndicator'                || 'ecommerceIndicator'
      'exceptionIndicatorType'            || 'exceptionIndicatorType'
      'freightOnboardType'                || 'freightOnboardType'
      'freightTermType'                   || 'freightTermType'
      'orderDate'                         || 'orderDate'
      'paymentTermType'                   || 'paymentTermType'
      'purchaseAgent'                     || 'purchaseAgent'
      'requiredDate'                      || 'requiredDate'
      'shipLocationType'                  || 'shipLocationType'
      'shipTo'                            || 'shipTo'
      'shipVia'                           || 'shipVia'
      'statusType'                        || 'statusType'
      'type'                              || 'type'
      'vendor'                            || 'vendor'
   }

   void "update valid purchase order" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      final def existingPurchaseOrder = purchaseOrderDataLoaderService.single(
         company,
         vendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn,
         customerAccountIn
      )
      final def updatedPurchaseOrderDTO = purchaseOrderDataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(vendorIn),
         approvedByIn,
         purchaseAgentIn,
         new SimpleIdentifiableDTO(shipViaIn),
         new SimpleIdentifiableDTO(shipToIn.myId()),
         new SimpleIdentifiableDTO(paymentTermTypeIn),
         vendorSubmittedEmployeeIn,
         new SimpleIdentifiableDTO(customerAccountIn)
      )
      updatedPurchaseOrderDTO.id = existingPurchaseOrder.id

      when:
      def result = put("$path/${existingPurchaseOrder.id}", updatedPurchaseOrderDTO)

      then:
      notThrown(HttpClientResponseException)
      result != null
      with(result) {
         id == updatedPurchaseOrderDTO.id
         number == updatedPurchaseOrderDTO.number
         vendor.id == updatedPurchaseOrderDTO.vendor.id

         with(statusType) {
            value == updatedPurchaseOrderDTO.statusType.value
            description == updatedPurchaseOrderDTO.statusType.description
         }

         orderDate == updatedPurchaseOrderDTO.orderDate.toString()

         with(type) {
            value == updatedPurchaseOrderDTO.type.value
            description == updatedPurchaseOrderDTO.type.description
         }

         with(freightOnboardType) {
            value == updatedPurchaseOrderDTO.freightOnboardType.value
            description == updatedPurchaseOrderDTO.freightOnboardType.description
         }

         with(freightTermType) {
            value == updatedPurchaseOrderDTO.freightTermType.value
            description == updatedPurchaseOrderDTO.freightTermType.description
         }

         with(shipLocationType) {
            value == updatedPurchaseOrderDTO.shipLocationType.value
            description == updatedPurchaseOrderDTO.shipLocationType.description
         }

         approvedBy.number == updatedPurchaseOrderDTO.approvedBy.number
         totalAmount == updatedPurchaseOrderDTO.totalAmount
         receivedAmount == updatedPurchaseOrderDTO.receivedAmount
         paidAmount == updatedPurchaseOrderDTO.paidAmount
         purchaseAgent.number == updatedPurchaseOrderDTO.purchaseAgent.number
         shipVia.id == updatedPurchaseOrderDTO.shipVia.id
         requiredDate == updatedPurchaseOrderDTO.requiredDate.toString()
         shipTo.id == updatedPurchaseOrderDTO.shipTo.myId()
         paymentTermType.id == updatedPurchaseOrderDTO.paymentTermType.id
         message == updatedPurchaseOrderDTO.message
         totalLandedAmount == updatedPurchaseOrderDTO.totalLandedAmount
         totalFreightAmount == updatedPurchaseOrderDTO.totalFreightAmount

         with(exceptionIndicatorType) {
            value == updatedPurchaseOrderDTO.exceptionIndicatorType.value
            description == updatedPurchaseOrderDTO.exceptionIndicatorType.description
         }

         vendorSubmittedTime.with { OffsetDateTime.parse(it) } == updatedPurchaseOrderDTO.vendorSubmittedTime.withOffsetSameInstant(ZoneOffset.UTC)
         vendorSubmittedEmployee.number == updatedPurchaseOrderDTO.vendorSubmittedEmployee.number
         ecommerceIndicator == updatedPurchaseOrderDTO.ecommerceIndicator
         customerAccount.id == updatedPurchaseOrderDTO.customerAccount.id
      }
   }

   void "update valid purchase order with nullable properties" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      final def existingPurchaseOrder = purchaseOrderDataLoaderService.single(
         company,
         vendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn,
         customerAccountIn
      )
      final def updatedPurchaseOrderDTO = purchaseOrderDataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(vendorIn),
         approvedByIn,
         purchaseAgentIn,
         new SimpleIdentifiableDTO(shipViaIn),
         new SimpleIdentifiableDTO(shipToIn.myId()),
         new SimpleIdentifiableDTO(paymentTermTypeIn),
         vendorSubmittedEmployeeIn,
         new SimpleIdentifiableDTO(customerAccountIn)
      )
      updatedPurchaseOrderDTO.id = existingPurchaseOrder.id
      updatedPurchaseOrderDTO.totalAmount = null
      updatedPurchaseOrderDTO.receivedAmount = null
      updatedPurchaseOrderDTO.paidAmount = null
      updatedPurchaseOrderDTO.message = null
      updatedPurchaseOrderDTO.totalLandedAmount = null
      updatedPurchaseOrderDTO.totalFreightAmount = null
      updatedPurchaseOrderDTO.vendorSubmittedTime = null
      updatedPurchaseOrderDTO.vendorSubmittedEmployee = null
      updatedPurchaseOrderDTO.customerAccount = null

      when:
      def result = put("$path/${existingPurchaseOrder.id}", updatedPurchaseOrderDTO)

      then:
      notThrown(HttpClientResponseException)
      result != null
      with(result) {
         id == updatedPurchaseOrderDTO.id
         number == updatedPurchaseOrderDTO.number
         vendor.id == updatedPurchaseOrderDTO.vendor.id

         with(statusType) {
            value == updatedPurchaseOrderDTO.statusType.value
            description == updatedPurchaseOrderDTO.statusType.description
         }

         orderDate == updatedPurchaseOrderDTO.orderDate.toString()

         with(type) {
            value == updatedPurchaseOrderDTO.type.value
            description == updatedPurchaseOrderDTO.type.description
         }

         with(freightOnboardType) {
            value == updatedPurchaseOrderDTO.freightOnboardType.value
            description == updatedPurchaseOrderDTO.freightOnboardType.description
         }

         with(freightTermType) {
            value == updatedPurchaseOrderDTO.freightTermType.value
            description == updatedPurchaseOrderDTO.freightTermType.description
         }

         with(shipLocationType) {
            value == updatedPurchaseOrderDTO.shipLocationType.value
            description == updatedPurchaseOrderDTO.shipLocationType.description
         }

         approvedBy.number == updatedPurchaseOrderDTO.approvedBy.number
         totalAmount == updatedPurchaseOrderDTO.totalAmount
         receivedAmount == updatedPurchaseOrderDTO.receivedAmount
         paidAmount == updatedPurchaseOrderDTO.paidAmount
         purchaseAgent.number == updatedPurchaseOrderDTO.purchaseAgent.number
         shipVia.id == updatedPurchaseOrderDTO.shipVia.id
         requiredDate == updatedPurchaseOrderDTO.requiredDate.toString()
         shipTo.id == updatedPurchaseOrderDTO.shipTo.myId()
         paymentTermType.id == updatedPurchaseOrderDTO.paymentTermType.id
         message == updatedPurchaseOrderDTO.message
         totalLandedAmount == updatedPurchaseOrderDTO.totalLandedAmount
         totalFreightAmount == updatedPurchaseOrderDTO.totalFreightAmount

         with(exceptionIndicatorType) {
            value == updatedPurchaseOrderDTO.exceptionIndicatorType.value
            description == updatedPurchaseOrderDTO.exceptionIndicatorType.description
         }

         vendorSubmittedTime == updatedPurchaseOrderDTO.vendorSubmittedTime
         vendorSubmittedEmployee == updatedPurchaseOrderDTO.vendorSubmittedEmployee
         ecommerceIndicator == updatedPurchaseOrderDTO.ecommerceIndicator
         customerAccount == updatedPurchaseOrderDTO.customerAccount
      }
   }

   @Unroll
   void "update invalid purchase order without #nonNullableProp" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      final def existingPurchaseOrder = purchaseOrderDataLoaderService.single(
         company,
         vendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn,
         customerAccountIn
      )
      final def updatedPurchaseOrderDTO = purchaseOrderDataLoaderService.singleDTO(
         new SimpleIdentifiableDTO(vendorIn),
         approvedByIn,
         purchaseAgentIn,
         new SimpleIdentifiableDTO(shipViaIn),
         new SimpleIdentifiableDTO(shipToIn.myId()),
         new SimpleIdentifiableDTO(paymentTermTypeIn),
         vendorSubmittedEmployeeIn,
         new SimpleIdentifiableDTO(customerAccountIn)
      )
      updatedPurchaseOrderDTO.id = existingPurchaseOrder.id
      updatedPurchaseOrderDTO["$nonNullableProp"] = null

      when:
      put("$path/${existingPurchaseOrder.id}", updatedPurchaseOrderDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp                     || errorResponsePath
      'approvedBy'                        || 'approvedBy'
      'ecommerceIndicator'                || 'ecommerceIndicator'
      'exceptionIndicatorType'            || 'exceptionIndicatorType'
      'freightOnboardType'                || 'freightOnboardType'
      'freightTermType'                   || 'freightTermType'
      'orderDate'                         || 'orderDate'
      'paymentTermType'                   || 'paymentTermType'
      'purchaseAgent'                     || 'purchaseAgent'
      'requiredDate'                      || 'requiredDate'
      'shipLocationType'                  || 'shipLocationType'
      'shipTo'                            || 'shipTo'
      'shipVia'                           || 'shipVia'
      'statusType'                        || 'statusType'
      'type'                              || 'type'
      'vendor'                            || 'vendor'
   }

   void "delete purchase order" () {
      given:
      final company = nineNineEightEmployee.company
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final shipViaList = shipViaFactoryService.stream(2, company).toList()
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPaymentTerm, vendorShipVia)
      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final shipViaIn = shipViaList[1]
      final shipToIn = storeFactoryService.store(3, company)
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      final customerAccountIn = accountDataLoaderService.single(company)
      final def purchaseOrder = purchaseOrderDataLoaderService.single(
         company,
         vendorIn,
         approvedByIn,
         purchaseAgentIn,
         shipViaIn,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn,
         customerAccountIn
      )

      when:
      delete("$path/${purchaseOrder.id}", )

      then: "purchase order for user's company is deleted"
      notThrown(HttpClientResponseException)

      when:
      get("$path/${purchaseOrder.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "${purchaseOrder.id} was unable to be found"
   }
}
