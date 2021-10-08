package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermDTO
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleDTO
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class AccountPayableInvoiceControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/accounting/account-payable/invoice"

   @Inject AccountPayableInvoiceDataLoaderService dataLoaderService
   @Inject PurchaseOrderTestDataLoaderService purchaseOrderDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService

   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final apInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, employeeIn, payToIn, store)

      when:
      def result = get("$path/${apInvoice.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == apInvoice.id
         vendor.id == apInvoice.vendor.myId()
         invoice == apInvoice.invoice
         purchaseOrder.id == apInvoice.purchaseOrder.myId()
         invoiceDate == apInvoice.invoiceDate.toString()
         invoiceAmount == apInvoice.invoiceAmount
         discountAmount == apInvoice.discountAmount
         discountPercent == apInvoice.discountPercent
         autoDistributionApplied == apInvoice.autoDistributionApplied
         discountTaken == apInvoice.discountTaken
         entryDate == apInvoice.entryDate.toString()
         expenseDate == apInvoice.expenseDate.toString()
         discountDate == apInvoice.discountDate.toString()
         employee.number == apInvoice.employee.number
         originalInvoiceAmount == apInvoice.originalInvoiceAmount
         message == apInvoice.message

         with(selected) {
            value == apInvoice.selected.value
            description == apInvoice.selected.description
         }

         multiplePaymentIndicator == apInvoice.multiplePaymentIndicator
         paidAmount == apInvoice.paidAmount
         selectedAmount == apInvoice.selectedAmount

         with(type) {
            value == apInvoice.type.value
            description == apInvoice.type.description
         }

         with(status) {
            value == apInvoice.status.value
            description == apInvoice.status.description
         }

         dueDate == apInvoice.dueDate.toString()
         payTo.id == apInvoice.payTo.myId()
         separateCheckIndicator == apInvoice.separateCheckIndicator
         useTaxIndicator == apInvoice.useTaxIndicator
         receiveDate == apInvoice.receiveDate.toString()
         location.id == apInvoice.location.myId()
      }
   }

   void "fetch one not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("$path/$nonExistentId")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def apInvoices = dataLoaderService.stream(20, company, vendorIn, purchaseOrderIn, employeeIn, payToIn, store)
         .map { new AccountPayableInvoiceDTO(it)}
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()
      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(4, 5, "id", "ASC")
      def pageFive = new StandardPageRequest(5, 5, "id", "ASC")
      def firstPage = apInvoices[0..4]
      def secondPage = apInvoices[5..9]
      def lastPage = apInvoices[15..19]

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
               vendor.id == firstPage[index].vendor.id
               invoice == firstPage[index].invoice
               purchaseOrder.id == firstPage[index].purchaseOrder.id
               invoiceDate == firstPage[index].invoiceDate.toString()
               invoiceAmount == firstPage[index].invoiceAmount
               discountAmount == firstPage[index].discountAmount
               discountPercent == firstPage[index].discountPercent
               autoDistributionApplied == firstPage[index].autoDistributionApplied
               discountTaken == firstPage[index].discountTaken
               entryDate == firstPage[index].entryDate.toString()
               expenseDate == firstPage[index].expenseDate.toString()
               discountDate == firstPage[index].discountDate.toString()
               employee.number == firstPage[index].employee.number
               originalInvoiceAmount == firstPage[index].originalInvoiceAmount
               message == firstPage[index].message

               with(selected) {
                  value == firstPage[index].selected.value
                  description == firstPage[index].selected.description
               }

               multiplePaymentIndicator == firstPage[index].multiplePaymentIndicator
               paidAmount == firstPage[index].paidAmount
               selectedAmount == firstPage[index].selectedAmount

               with(type) {
                  value == firstPage[index].type.value
                  description == firstPage[index].type.description
               }

               with(status) {
                  value == firstPage[index].status.value
                  description == firstPage[index].status.description
               }

               dueDate == firstPage[index].dueDate.toString()
               payTo.id == firstPage[index].payTo.id
               separateCheckIndicator == firstPage[index].separateCheckIndicator
               useTaxIndicator == firstPage[index].useTaxIndicator
               receiveDate == firstPage[index].receiveDate.toString()
               location.id == firstPage[index].location.id
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
               vendor.id == secondPage[index].vendor.id
               invoice == secondPage[index].invoice
               purchaseOrder.id == secondPage[index].purchaseOrder.id
               invoiceDate == secondPage[index].invoiceDate.toString()
               invoiceAmount == secondPage[index].invoiceAmount
               discountAmount == secondPage[index].discountAmount
               discountPercent == secondPage[index].discountPercent
               autoDistributionApplied == secondPage[index].autoDistributionApplied
               discountTaken == secondPage[index].discountTaken
               entryDate == secondPage[index].entryDate.toString()
               expenseDate == secondPage[index].expenseDate.toString()
               discountDate == secondPage[index].discountDate.toString()
               employee.number == secondPage[index].employee.number
               originalInvoiceAmount == secondPage[index].originalInvoiceAmount
               message == secondPage[index].message

               with(selected) {
                  value == secondPage[index].selected.value
                  description == secondPage[index].selected.description
               }

               multiplePaymentIndicator == secondPage[index].multiplePaymentIndicator
               paidAmount == secondPage[index].paidAmount
               selectedAmount == secondPage[index].selectedAmount

               with(type) {
                  value == secondPage[index].type.value
                  description == secondPage[index].type.description
               }

               with(status) {
                  value == secondPage[index].status.value
                  description == secondPage[index].status.description
               }

               dueDate == secondPage[index].dueDate.toString()
               payTo.id == secondPage[index].payTo.id
               separateCheckIndicator == secondPage[index].separateCheckIndicator
               useTaxIndicator == secondPage[index].useTaxIndicator
               receiveDate == secondPage[index].receiveDate.toString()
               location.id == secondPage[index].location.id
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
               vendor.id == lastPage[index].vendor.id
               invoice == lastPage[index].invoice
               purchaseOrder.id == lastPage[index].purchaseOrder.id
               invoiceDate == lastPage[index].invoiceDate.toString()
               invoiceAmount == lastPage[index].invoiceAmount
               discountAmount == lastPage[index].discountAmount
               discountPercent == lastPage[index].discountPercent
               autoDistributionApplied == lastPage[index].autoDistributionApplied
               discountTaken == lastPage[index].discountTaken
               entryDate == lastPage[index].entryDate.toString()
               expenseDate == lastPage[index].expenseDate.toString()
               discountDate == lastPage[index].discountDate.toString()
               employee.number == lastPage[index].employee.number
               originalInvoiceAmount == lastPage[index].originalInvoiceAmount
               message == lastPage[index].message

               with(selected) {
                  value == lastPage[index].selected.value
                  description == lastPage[index].selected.description
               }

               multiplePaymentIndicator == lastPage[index].multiplePaymentIndicator
               paidAmount == lastPage[index].paidAmount
               selectedAmount == lastPage[index].selectedAmount

               with(type) {
                  value == lastPage[index].type.value
                  description == lastPage[index].type.description
               }

               with(status) {
                  value == lastPage[index].status.value
                  description == lastPage[index].status.description
               }

               dueDate == lastPage[index].dueDate.toString()
               payTo.id == lastPage[index].payTo.id
               separateCheckIndicator == lastPage[index].separateCheckIndicator
               useTaxIndicator == lastPage[index].useTaxIndicator
               receiveDate == lastPage[index].receiveDate.toString()
               location.id == lastPage[index].location.id
            }
         }
      }

      when:
      get("$path/${pageFive}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final apInvoiceDTO = dataLoaderService.singleDTO(
         company,
         new SimpleIdentifiableDTO(vendorIn),
         new SimpleIdentifiableDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new SimpleIdentifiableDTO(payToIn),
         new SimpleLegacyIdentifiableDTO(store.myId())
      )

      when:
      def result = post("$path/", apInvoiceDTO)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         vendor.id == apInvoiceDTO.vendor.id
         invoice == apInvoiceDTO.invoice
         purchaseOrder.id == apInvoiceDTO.purchaseOrder.id
         invoiceDate == apInvoiceDTO.invoiceDate.toString()
         invoiceAmount == apInvoiceDTO.invoiceAmount
         discountAmount == apInvoiceDTO.discountAmount
         discountPercent == apInvoiceDTO.discountPercent
         autoDistributionApplied == apInvoiceDTO.autoDistributionApplied
         discountTaken == apInvoiceDTO.discountTaken
         entryDate == apInvoiceDTO.entryDate.toString()
         expenseDate == apInvoiceDTO.expenseDate.toString()
         discountDate == apInvoiceDTO.discountDate.toString()
         employee.number == apInvoiceDTO.employee.number
         originalInvoiceAmount == apInvoiceDTO.originalInvoiceAmount
         message == apInvoiceDTO.message

         with(selected) {
            value == apInvoiceDTO.selected.value
            description == apInvoiceDTO.selected.description
         }

         multiplePaymentIndicator == apInvoiceDTO.multiplePaymentIndicator
         paidAmount == apInvoiceDTO.paidAmount
         selectedAmount == apInvoiceDTO.selectedAmount

         with(type) {
            value == apInvoiceDTO.type.value
            description == apInvoiceDTO.type.description
         }

         with(status) {
            value == apInvoiceDTO.status.value
            description == apInvoiceDTO.status.description
         }

         dueDate == apInvoiceDTO.dueDate.toString()
         payTo.id == apInvoiceDTO.payTo.id
         separateCheckIndicator == apInvoiceDTO.separateCheckIndicator
         useTaxIndicator == apInvoiceDTO.useTaxIndicator
         receiveDate == apInvoiceDTO.receiveDate.toString()
         location.id == apInvoiceDTO.location.id
      }
   }

   void "create valid account payable invoice without nullable properties" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final apInvoiceDTO = dataLoaderService.singleDTO(
         company,
         new SimpleIdentifiableDTO(vendorIn),
         new SimpleIdentifiableDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new SimpleIdentifiableDTO(payToIn),
         new SimpleLegacyIdentifiableDTO(store.myId())
      )
      apInvoiceDTO.purchaseOrder = null
      apInvoiceDTO.location = null

      when:
      def result = post("$path/", apInvoiceDTO)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      with(result) {
         id != null
         vendor.id == apInvoiceDTO.vendor.id
         invoice == apInvoiceDTO.invoice
         purchaseOrder.id == apInvoiceDTO.purchaseOrder
         invoiceDate == apInvoiceDTO.invoiceDate.toString()
         invoiceAmount == apInvoiceDTO.invoiceAmount
         discountAmount == apInvoiceDTO.discountAmount
         discountPercent == apInvoiceDTO.discountPercent
         autoDistributionApplied == apInvoiceDTO.autoDistributionApplied
         discountTaken == apInvoiceDTO.discountTaken
         entryDate == apInvoiceDTO.entryDate.toString()
         expenseDate == apInvoiceDTO.expenseDate.toString()
         discountDate == apInvoiceDTO.discountDate.toString()
         employee.number == apInvoiceDTO.employee.number
         originalInvoiceAmount == apInvoiceDTO.originalInvoiceAmount
         message == apInvoiceDTO.message

         with(selected) {
            value == apInvoiceDTO.selected.value
            description == apInvoiceDTO.selected.description
         }

         multiplePaymentIndicator == apInvoiceDTO.multiplePaymentIndicator
         paidAmount == apInvoiceDTO.paidAmount
         selectedAmount == apInvoiceDTO.selectedAmount

         with(type) {
            value == apInvoiceDTO.type.value
            description == apInvoiceDTO.type.description
         }

         with(status) {
            value == apInvoiceDTO.status.value
            description == apInvoiceDTO.status.description
         }

         dueDate == apInvoiceDTO.dueDate.toString()
         payTo.id == apInvoiceDTO.payTo.id
         separateCheckIndicator == apInvoiceDTO.separateCheckIndicator
         useTaxIndicator == apInvoiceDTO.useTaxIndicator
         receiveDate == apInvoiceDTO.receiveDate.toString()
         location.id == apInvoiceDTO.location
      }
   }

   @Unroll
   void "create invalid account payable invoice without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final apInvoiceDTO = dataLoaderService.singleDTO(
         company,
         new SimpleIdentifiableDTO(vendorIn),
         new SimpleIdentifiableDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new SimpleIdentifiableDTO(payToIn),
         new SimpleLegacyIdentifiableDTO(store.myId())
      )
      apInvoiceDTO["$nonNullableProp"] = null

      when:
      post("$path/", apInvoiceDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'
      response[0].code == 'javax.validation.constraints.NotNull.message'

      where:
      nonNullableProp                  || errorResponsePath
      'vendor'                         || 'vendor'
      'invoice'                        || 'invoice'
      'invoiceDate'                    || 'invoiceDate'
      'invoiceAmount'                  || 'invoiceAmount'
      'autoDistributionApplied'        || 'autoDistributionApplied'
      'entryDate'                      || 'entryDate'
      'expenseDate'                    || 'expenseDate'
      'employee'                       || 'employee'
      'originalInvoiceAmount'          || 'originalInvoiceAmount'
      'selected'                       || 'selected'
      'multiplePaymentIndicator'       || 'multiplePaymentIndicator'
      'paidAmount'                     || 'paidAmount'
      'type'                           || 'type'
      'status'                         || 'status'
      'dueDate'                        || 'dueDate'
      'payTo'                          || 'payTo'
      'useTaxIndicator'                || 'useTaxIndicator'
   }

   @Unroll
   void "create invalid account payable invoice with non-existing #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final apInvoiceDTO = dataLoaderService.singleDTO(
         company,
         new SimpleIdentifiableDTO(vendorIn),
         new SimpleIdentifiableDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new SimpleIdentifiableDTO(payToIn),
         new SimpleLegacyIdentifiableDTO(store.myId())
      )
      apInvoiceDTO["$testProp"] = invalidValue

      when:
      post("$path", apInvoiceDTO)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      response[0].code == errorCode

      where:
      testProp          | invalidValue                                                                       || errorResponsePath  | errorCode                                         | errorMessage
      'vendor'          | new SimpleIdentifiableDTO(UUID.fromString('905545bf-3509-4ad3-8ccc-e437b2dbdcb0')) || 'vendor.id'        | 'system.not.found'                                | "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      'purchaseOrder'   | new SimpleIdentifiableDTO(UUID.fromString('905545bf-3509-4ad3-8ccc-e437b2dbdcb0')) || 'purchaseOrder.id' | 'system.not.found'                                | "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      'selected'        | new AccountPayableInvoiceSelectedTypeDTO('Z', 'Invalid DTO')                       || 'selected.value'   | 'system.not.found'                                | "Z was unable to be found"
      'type'            | new AccountPayableInvoiceTypeDTO('Z', 'Invalid DTO')                               || 'type.value'       | 'system.not.found'                                | "Z was unable to be found"
      'status'          | new AccountPayableInvoiceStatusTypeDTO('Z', 'Invalid DTO')                         || 'status.value'     | 'system.not.found'                                | "Z was unable to be found"
      'payTo'           | new SimpleIdentifiableDTO(UUID.fromString('905545bf-3509-4ad3-8ccc-e437b2dbdcb0')) || 'payTo.id'         | 'system.not.found'                                | "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      'location'        | new SimpleLegacyIdentifiableDTO(0)                                                 || 'location.id'      | 'system.not.found'                                | "0 was unable to be found"
      'discountPercent' | -0.1212345                                                                         || 'discountPercent'  | 'javax.validation.constraints.DecimalMin.message' | 'must be greater than or equal to value'
      'discountPercent' | 0.12123456                                                                         || 'discountPercent'  | 'javax.validation.constraints.Digits.message'     | '0.12123456 is out of range for discountPercent'
      'discountPercent' | 1                                                                                  || 'discountPercent'  | 'javax.validation.constraints.DecimalMax.message' | 'must be less than or equal to value'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, employeeIn, payToIn, store)
      final updatedAPInvoice = dataLoaderService.singleDTO(
         company,
         new SimpleIdentifiableDTO(vendorIn),
         new SimpleIdentifiableDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new SimpleIdentifiableDTO(payToIn),
         new SimpleLegacyIdentifiableDTO(store.myId())
      )
      updatedAPInvoice.id = existingAPInvoice.id

      when:
      def result = put("$path/${existingAPInvoice.id}", updatedAPInvoice)

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id != null
         id == updatedAPInvoice.id
         vendor.id == updatedAPInvoice.vendor.id
         invoice == updatedAPInvoice.invoice
         purchaseOrder.id == updatedAPInvoice.purchaseOrder.id
         invoiceDate == updatedAPInvoice.invoiceDate.toString()
         invoiceAmount == updatedAPInvoice.invoiceAmount
         discountAmount == updatedAPInvoice.discountAmount
         discountPercent == updatedAPInvoice.discountPercent
         autoDistributionApplied == updatedAPInvoice.autoDistributionApplied
         discountTaken == updatedAPInvoice.discountTaken
         entryDate == updatedAPInvoice.entryDate.toString()
         expenseDate == updatedAPInvoice.expenseDate.toString()
         discountDate == updatedAPInvoice.discountDate.toString()
         employee.number == updatedAPInvoice.employee.number
         originalInvoiceAmount == updatedAPInvoice.originalInvoiceAmount
         message == updatedAPInvoice.message

         with(selected) {
            value == updatedAPInvoice.selected.value
            description == updatedAPInvoice.selected.description
         }

         multiplePaymentIndicator == updatedAPInvoice.multiplePaymentIndicator
         paidAmount == updatedAPInvoice.paidAmount
         selectedAmount == updatedAPInvoice.selectedAmount

         with(type) {
            value == updatedAPInvoice.type.value
            description == updatedAPInvoice.type.description
         }

         with(status) {
            value == updatedAPInvoice.status.value
            description == updatedAPInvoice.status.description
         }

         dueDate == updatedAPInvoice.dueDate.toString()
         payTo.id == updatedAPInvoice.payTo.id
         separateCheckIndicator == updatedAPInvoice.separateCheckIndicator
         useTaxIndicator == updatedAPInvoice.useTaxIndicator
         receiveDate == updatedAPInvoice.receiveDate.toString()
         location.id == updatedAPInvoice.location.myId()
      }
   }

   void "update valid account payable invoice without nullable properties" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, employeeIn, payToIn, store)
      final updatedAPInvoice = dataLoaderService.singleDTO(
         company,
         new SimpleIdentifiableDTO(vendorIn),
         new SimpleIdentifiableDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new SimpleIdentifiableDTO(payToIn),
         new SimpleLegacyIdentifiableDTO(store.myId())
      )
      updatedAPInvoice.id = existingAPInvoice.id
      updatedAPInvoice.purchaseOrder = null
      updatedAPInvoice.location = null

      when:
      def result = put("$path/${existingAPInvoice.id}", updatedAPInvoice)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      with(result) {
         id == updatedAPInvoice.id
         vendor.id == updatedAPInvoice.vendor.id
         invoice == updatedAPInvoice.invoice
         purchaseOrder.id == updatedAPInvoice.purchaseOrder
         invoiceDate == updatedAPInvoice.invoiceDate.toString()
         invoiceAmount == updatedAPInvoice.invoiceAmount
         discountAmount == updatedAPInvoice.discountAmount
         discountPercent == updatedAPInvoice.discountPercent
         autoDistributionApplied == updatedAPInvoice.autoDistributionApplied
         discountTaken == updatedAPInvoice.discountTaken
         entryDate == updatedAPInvoice.entryDate.toString()
         expenseDate == updatedAPInvoice.expenseDate.toString()
         discountDate == updatedAPInvoice.discountDate.toString()
         employee.number == updatedAPInvoice.employee.number
         originalInvoiceAmount == updatedAPInvoice.originalInvoiceAmount
         message == updatedAPInvoice.message

         with(selected) {
            value == updatedAPInvoice.selected.value
            description == updatedAPInvoice.selected.description
         }

         multiplePaymentIndicator == updatedAPInvoice.multiplePaymentIndicator
         paidAmount == updatedAPInvoice.paidAmount
         selectedAmount == updatedAPInvoice.selectedAmount

         with(type) {
            value == updatedAPInvoice.type.value
            description == updatedAPInvoice.type.description
         }

         with(status) {
            value == updatedAPInvoice.status.value
            description == updatedAPInvoice.status.description
         }

         dueDate == updatedAPInvoice.dueDate.toString()
         payTo.id == updatedAPInvoice.payTo.id
         separateCheckIndicator == updatedAPInvoice.separateCheckIndicator
         useTaxIndicator == updatedAPInvoice.useTaxIndicator
         receiveDate == updatedAPInvoice.receiveDate.toString()
         location.id == updatedAPInvoice.location
      }
   }

   @Unroll
   void "update invalid account payable invoice without #nonNullableProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, employeeIn, payToIn, store)
      final updatedAPInvoice = dataLoaderService.singleDTO(
         company,
         new SimpleIdentifiableDTO(vendorIn),
         new SimpleIdentifiableDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new SimpleIdentifiableDTO(payToIn),
         new SimpleLegacyIdentifiableDTO(store.myId())
      )
      updatedAPInvoice.id = existingAPInvoice.id
      updatedAPInvoice["$nonNullableProp"] = null

      when:
      put("$path/${existingAPInvoice.id}", updatedAPInvoice)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'
      response[0].code == 'javax.validation.constraints.NotNull.message'

      where:
      nonNullableProp                  || errorResponsePath
      'vendor'                         || 'vendor'
      'invoice'                        || 'invoice'
      'invoiceDate'                    || 'invoiceDate'
      'invoiceAmount'                  || 'invoiceAmount'
      'autoDistributionApplied'        || 'autoDistributionApplied'
      'entryDate'                      || 'entryDate'
      'expenseDate'                    || 'expenseDate'
      'employee'                       || 'employee'
      'originalInvoiceAmount'          || 'originalInvoiceAmount'
      'selected'                       || 'selected'
      'multiplePaymentIndicator'       || 'multiplePaymentIndicator'
      'paidAmount'                     || 'paidAmount'
      'type'                           || 'type'
      'status'                         || 'status'
      'dueDate'                        || 'dueDate'
      'payTo'                          || 'payTo'
      'useTaxIndicator'                || 'useTaxIndicator'
      }

   @Unroll
   void "update invalid account payable invoice with non-existing #testProp" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final store = storeFactoryService.store(3, company)
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, employeeIn, payToIn, store)
      final updatedAPInvoice = dataLoaderService.singleDTO(
         company,
         new SimpleIdentifiableDTO(vendorIn),
         new SimpleIdentifiableDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new SimpleIdentifiableDTO(payToIn),
         new SimpleLegacyIdentifiableDTO(store.myId())
      )
      updatedAPInvoice.id = existingAPInvoice.id
      updatedAPInvoice["$testProp"] = invalidValue

      when:
      put("$path/${existingAPInvoice.id}", updatedAPInvoice)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      response[0].code == 'system.not.found'

      where:
      testProp        | invalidValue                                                                       || errorResponsePath  | errorMessage
      'vendor'        | new SimpleIdentifiableDTO(UUID.fromString('905545bf-3509-4ad3-8ccc-e437b2dbdcb0')) || 'vendor.id'        | "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      'purchaseOrder' | new SimpleIdentifiableDTO(UUID.fromString('905545bf-3509-4ad3-8ccc-e437b2dbdcb0')) || 'purchaseOrder.id' | "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      'selected'      | new AccountPayableInvoiceSelectedTypeDTO('Z', 'Invalid DTO')                       || 'selected.value'   | "Z was unable to be found"
      'type'          | new AccountPayableInvoiceTypeDTO('Z', 'Invalid DTO')                               || 'type.value'       | "Z was unable to be found"
      'status'        | new AccountPayableInvoiceStatusTypeDTO('Z', 'Invalid DTO')                         || 'status.value'     | "Z was unable to be found"
      'payTo'         | new SimpleIdentifiableDTO(UUID.fromString('905545bf-3509-4ad3-8ccc-e437b2dbdcb0')) || 'payTo.id'         | "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      'location'      | new SimpleLegacyIdentifiableDTO(0)                                                 || 'location.id'      | "0 was unable to be found"
   }
}
