package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.AccountPayableCheckPreviewFilterRequest
import com.cynergisuite.domain.AccountPayableInvoiceInquiryFilterRequest
import com.cynergisuite.domain.AccountPayableInvoiceListByVendorFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDataLoader
import com.cynergisuite.domain.AccountPayableVendorBalanceReportFilterRequest
import com.cynergisuite.domain.ExpenseReportFilterRequest
import com.cynergisuite.domain.InvoiceReportFilterRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.domain.infrastructure.SimpleTransactionalSql
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDetailDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateDTO
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceMaintenanceDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDataLoader
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.general.ledger.control.GeneralLedgerControlDataLoaderService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth

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
   @Inject BankFactoryService bankFactoryService
   @Inject AccountTestDataLoaderService accountFactoryService
   @Inject AccountPayablePaymentDataLoaderService accountPayablePaymentDataLoaderService
   @Inject AccountPayablePaymentDetailDataLoaderService apPaymentDetailDataLoaderService
   @Inject AccountPayableDistributionDetailDataLoaderService apDistDetailDataLoaderService
   @Inject AccountPayableDistributionTemplateDataLoaderService apDistTemplateDataLoaderService
   @Inject SimpleTransactionalSql sql
   @Inject AccountPayableControlTestDataLoaderService accountPayableControlDataLoaderService
   @Inject GeneralLedgerControlDataLoaderService generalLedgerControlDataLoaderService


   void "fetch one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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

      final apInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)

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
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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

      def apInvoices = dataLoaderService.stream(20, company, vendorIn, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)
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

   void "fetch all by vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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

      def apInvoices = dataLoaderService.stream(20, company, vendorIn, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)
         .map { new AccountPayableInvoiceDTO(it)}
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()

      def filterRequest = new AccountPayableInvoiceListByVendorFilterRequest()
      switch (criteria) {
         case 'Search by vendor':
            filterRequest['vendor'] = vendorIn.number
            break
         case 'Search by invoice':
            filterRequest['invoice'] = apInvoices[2].invoice
            break
         case 'Search by both':
            filterRequest['vendor'] = vendorIn.number
            filterRequest['invoice'] = apInvoices[10].invoice
            break
         case 'Search by neither':
            break
      }

      when:
      def result = get("$path/list-by-vendor${filterRequest}")

      then:
      notThrown(Exception)
      result != null
      result.totalElements == elements

      where:
      criteria              || elements
      'Search by vendor'    || 20
      'Search by invoice'   || 20
      'Search by both'      || 10
      'Search by neither'   || 20
   }

   void "fetch open by vendor" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorPmtTerm = vendorPaymentTermList[0]
      final vendorShipVia = shipViaList[0]
      final vendorOne = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)
      final vendorTwo = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)
      final vendorThree = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      final poVendorPmtTerm = vendorPaymentTermList[1]
      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poPmtTerm = vendorPaymentTermList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderOne = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      final purchaseOrderTwo = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      final purchaseOrderThree = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def openAccountPayableInvoiceStatus = AccountPayableInvoiceStatusTypeDataLoader.predefined().get(1)
      def paidAccountPayableInvoiceStatus = AccountPayableInvoiceStatusTypeDataLoader.predefined().get(2)

      def apInvoicesVendorOneOpen20 = dataLoaderService.stream(20, company, vendorOne, purchaseOrderOne, null, employeeIn, null, openAccountPayableInvoiceStatus, payToIn, store)
         .map { new AccountPayableInvoiceDTO(it)}
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()

      def apInvoicesVendorOnePaid15 = dataLoaderService.stream(15, company, vendorOne, purchaseOrderTwo, null, employeeIn, null, paidAccountPayableInvoiceStatus, payToIn, store)
         .map { new AccountPayableInvoiceDTO(it)}
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()

      def apInvoicesVendorTwoOpen10 = dataLoaderService.stream(10, company, vendorTwo, purchaseOrderThree, null, employeeIn, null, openAccountPayableInvoiceStatus, payToIn, store)
         .map { new AccountPayableInvoiceDTO(it)}
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()

      def filterRequest = new AccountPayableInvoiceListByVendorFilterRequest()
      switch (criteria) {
         case 'Search by first vendor with 20 open and 15 paid':
            filterRequest['vendor'] = vendorOne.number
            break
         case 'Search by second vendor with 10 open':
            filterRequest['vendor'] = vendorTwo.number
            break
         case 'Search by third vendor with 0 open':
            filterRequest['vendor'] = vendorThree.number
            break
      }

      when:
      def result = get("$path/open-by-vendor${filterRequest}")

      then:
      notThrown(Exception)
      result != null
      result.totalElements == elements

      where:
      criteria                                              || elements
      'Search by first vendor with 20 open and 15 paid'     || 20
      'Search by second vendor with 10 open'                || 10
      'Search by third vendor with 0 open'                  || 0
   }

   void "fetch AP Invoice report" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(1, company) as StoreEntity
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
      final statusO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")

      def apInvoiceEntities = dataLoaderService.stream(20, company, vendorIn, purchaseOrderIn, null, employeeIn, null, statusO, payToIn, store)
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()

      def apInvoices = apInvoiceEntities.stream().map { new AccountPayableInvoiceDTO(it) }.toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def pmtStatuses = AccountPayablePaymentStatusTypeDataLoader.predefined()
      def pmtTypes = AccountPayablePaymentTypeTypeDataLoader.predefined()

      def apPayments = accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }).toList()
      apPayments.addAll(accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'V' }, pmtTypes.find { it.value == 'C' }, null, null, null, true).toList())


      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntities[0], apPayments[0]).toList()
      apPaymentDetails.addAll(apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntities[0], apPayments[1]).toList())
      apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntities[1], apPayments[2]).toList()
      apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntities[1], apPayments[3]).toList()

      def template = apDistTemplateDataLoaderService.single(company)
      def apDistribution = apDistDetailDataLoaderService.single(store, account, company, template)

      sql.executeUpdate([invoice_id: apInvoices[0].id, account_id: account.id, profit_center_sfk: store.number, amount: 1000], """
         INSERT INTO account_payable_invoice_distribution(invoice_id, distribution_account_id, distribution_profit_center_id_sfk, distribution_amount)
	      VALUES (:invoice_id, :account_id, :profit_center_sfk, :amount)
         """)
      sql.executeUpdate([invoice_id: apInvoices[1].id, account_id: account.id, profit_center_sfk: store.number, amount: 2000], """
         INSERT INTO account_payable_invoice_distribution(invoice_id, distribution_account_id, distribution_profit_center_id_sfk, distribution_amount)
	      VALUES (:invoice_id, :account_id, :profit_center_sfk, :amount)
         """)

      def filterRequest = new InvoiceReportFilterRequest([sortBy: "poHeader.number", sortDirection: "ASC", invStatus: ["O", "P"]])

      when:
      def result = get("$path/report${filterRequest}")

      then:
      notThrown(Exception)
      with(result) {
         expenseTotal > 0
         paidTotal > 0
         purchaseOrders.eachWithIndex { purchaseOrder, index ->
            poHeaderNumber == purchaseOrderIn.number
            totalPoInventoryCost > 0
            totalPoDistributions > 0
            with(purchaseOrder.invoices[0]) {
               id == apInvoices[index].id
               vendorNumber == vendorIn.number
               vendorName == vendorIn.name
               invoice == apInvoices[index].invoice
               operator == apInvoices[index].employee.number
               useTax == apInvoices[index].useTaxIndicator
               type == apInvoices[index].type.value
               invoiceDate == apInvoices[index].invoiceDate.toString()
               entryDate == apInvoices[index].entryDate.toString()
               status == apInvoices[index].status.value
               invoiceAmount == apInvoices[index].invoiceAmount
               discountTaken == apInvoices[index].discountTaken
               dueDate == apInvoices[index].dueDate.toString()
               expenseDate == apInvoices[index].expenseDate.toString()
               paidAmount == apInvoices[index].paidAmount
               bankNumber == bank.number
               pmtType == apPayments[0].type.value
               pmtNumber == apPayments[0].paymentNumber
               notes == apInvoices[index].message
               with(invoiceDetails[0]) {
                  paymentNumber == apPayments[0].paymentNumber
                  paymentDetailId == apPaymentDetails[0].id.toString()
                  paymentDetailAmount == apPaymentDetails[0].amount
               }
               with(invoiceDetails[9]) {
                  paymentNumber == apPayments[1].paymentNumber
                  paymentDetailId == apPaymentDetails[9].id.toString()
                  paymentDetailAmount == apPaymentDetails[9].amount
               }
               with(distDetails[0]) {
                  accountNumber == account.number
                  accountName == account.name
                  distProfitCenter == store.number
                  distAmount == 1000
               }
               inventories.size() == 12
               with(inventories[0]) {
                  invoiceNumber == '9100029365'
                  modelNumber == 'TR'
               }
               with(inventories[11]) {
                  invoiceNumber == '9100029365'
                  modelNumber == 'TR'
               }
            }
         }
      }
   }

   void "fetch AP invoice inquiry" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(1, company) as StoreEntity
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
      final purchaseOrderIn1 = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      final purchaseOrderIn2 = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      final purchaseOrderIn3 = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      final purchaseOrderIn4 = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      final purchaseOrderIn5 = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final statusO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")
      final statusP = new AccountPayableInvoiceStatusType(3, "P", "Paid", "paid")

      final payToPmtTerm = vendorPaymentTermList[3]
      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def apInvoiceEntities = dataLoaderService.stream(4, company, vendorIn, payToIn, purchaseOrderIn1, LocalDate.of(2020, 1, 1), 1000 as BigDecimal, employeeIn, null, statusO, LocalDate.of(2020, 1, 2), store)
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()
      apInvoiceEntities.addAll(dataLoaderService.stream(4, company, vendorIn, payToIn, purchaseOrderIn2, LocalDate.of(2020, 2, 1), 2000 as BigDecimal, employeeIn, null, statusP, LocalDate.of(2020, 2, 2), store)
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList())
      apInvoiceEntities.addAll(dataLoaderService.stream(4, company, vendorIn, payToIn, purchaseOrderIn3, LocalDate.of(2020, 3, 1), 3000 as BigDecimal, employeeIn, null, statusO, LocalDate.of(2020, 3, 2), store)
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList())
      apInvoiceEntities.addAll(dataLoaderService.stream(4, company, vendorIn, payToIn, purchaseOrderIn4, LocalDate.of(2020, 4, 1), 4000 as BigDecimal, employeeIn, null, statusP, LocalDate.of(2020, 4, 2), store)
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList())
      apInvoiceEntities.addAll(dataLoaderService.stream(4, company, vendorIn, payToIn, purchaseOrderIn5, LocalDate.of(2020, 5, 1), 5000 as BigDecimal, employeeIn, null, statusO, LocalDate.of(2020, 5, 2), store)
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList())

      def filterRequest = new AccountPayableInvoiceInquiryFilterRequest()
      filterRequest['vendor'] = vendorIn.number
      filterRequest['payTo'] = payToIn.number
      switch (criteria) {
         case 'Search by status':
            filterRequest['invStatus'] = 'P'
            filterRequest['poNbr'] = purchaseOrderIn2.number
            filterRequest['sortBy'] = 'poHeader.number'
            break
         case 'Search by purchase order number':
            filterRequest['poNbr'] = purchaseOrderIn5.number
            filterRequest['sortBy'] = 'poHeader.number'
            break
         case 'Search by invoice number':
            filterRequest['invNbr'] = apInvoiceEntities[2].invoice
            filterRequest['sortBy'] = 'apInvoice.invoice'
            break
         case 'Search by invoice date':
            filterRequest['invDate'] = LocalDate.of(2020, 3, 1)
            filterRequest['sortBy'] = 'apInvoice.invoice_date'
            break
         case 'Search by due date':
            filterRequest['dueDate'] = LocalDate.of(2020, 2, 2)
            filterRequest['sortBy'] = 'apInvoice.due_date'
            break
         case 'Search by invoice amount':
            filterRequest['invAmount'] = 2000
            filterRequest['sortBy'] = 'apInvoice.invoice_amount'
            break
      }

      when:
      def result = get("$path/inquiry${filterRequest}")

      then:
      notThrown(Exception)
      result != null
      result.totalElements == elements

      where:
      criteria                            || elements
      'Search by status'                  || 4
      'Search by purchase order number'   || 4
      'Search by invoice number'          || 5
      'Search by invoice date'            || 4
      'Search by due date'                || 4
      'Search by invoice amount'          || 4
   }

   void "fetch AP Expense report" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(1, company) as StoreEntity
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
      final statusO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")

      def apInvoiceEntities = dataLoaderService.stream(20, company, vendorIn, purchaseOrderIn, null, employeeIn, null, statusO, payToIn, store)
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()

      def apInvoices = apInvoiceEntities.stream().map { new AccountPayableInvoiceDTO(it) }.toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def pmtStatuses = AccountPayablePaymentStatusTypeDataLoader.predefined()
      def pmtTypes = AccountPayablePaymentTypeTypeDataLoader.predefined()

      def apPayments = accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }).toList()
      apPayments.addAll(accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'V' }, pmtTypes.find { it.value == 'C' }, null, null, null, true).toList())

      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(1, company, vendorIn, apInvoiceEntities[0], apPayments[0], 2000).toList()
      apPaymentDetailDataLoaderService.stream(1, company, vendorIn, apInvoiceEntities[1], apPayments[1], 4000).toList()
      apPaymentDetailDataLoaderService.stream(1, company, vendorIn, apInvoiceEntities[1], apPayments[2], 5000).toList()

      def template = apDistTemplateDataLoaderService.single(company)
      def apDistribution = apDistDetailDataLoaderService.single(store, account, company, template)
      def invDistAmountForFirstInvoice = 1000
      def invDistAmountForSecondInvoice = 2000

      sql.executeUpdate([invoice_id: apInvoices[0].id, account_id: account.id, profit_center_sfk: store.number, amount: invDistAmountForFirstInvoice], """
         INSERT INTO account_payable_invoice_distribution(invoice_id, distribution_account_id, distribution_profit_center_id_sfk, distribution_amount)
	      VALUES (:invoice_id, :account_id, :profit_center_sfk, :amount)
         """)
      sql.executeUpdate([invoice_id: apInvoices[1].id, account_id: account.id, profit_center_sfk: store.number, amount: invDistAmountForSecondInvoice], """
         INSERT INTO account_payable_invoice_distribution(invoice_id, distribution_account_id, distribution_profit_center_id_sfk, distribution_amount)
	      VALUES (:invoice_id, :account_id, :profit_center_sfk, :amount)
         """)

      def filterRequest = new ExpenseReportFilterRequest([sortDirection: "ASC", invStatus: ["O", "P"], beginDate: LocalDate.of(2020, 1, 1), endDate: OffsetDateTime.now().toLocalDate()])

      when:
      def result = get("$path/expense/report${filterRequest}")

      then:
      notThrown(Exception)
      with(result.groupedByAccount[0]) {
         accountNumber == account.number
         accountName == account.name
         groupedByDistributionCenters.eachWithIndex { profitCenter, index ->
            with(profitCenter) {
               distCenter == store.number
               accountTotal == invDistAmountForFirstInvoice + invDistAmountForSecondInvoice
               with(invoices[0]) {
                  id == apInvoices[index].id
                  vendorNumber == vendorIn.number
                  vendorName == vendorIn.name
                  invoice == apInvoices[index].invoice
                  type == apInvoices[index].type.value
                  invoiceDate == apInvoices[index].invoiceDate.toString()
                  status == apInvoices[index].status.value
                  invoiceAmount == apInvoices[index].invoiceAmount
                  expenseDate == apInvoices[index].expenseDate.toString()
                  paidAmount == apInvoices[index].paidAmount
                  bankNumber == bank.number
                  pmtNumber == apPayments[0].paymentNumber
                  pmtDate == apPayments[0].paymentDate.toString()
                  notes == apInvoices[index].message
                  poHeaderNumber == purchaseOrderIn.number
                  acctNumber == account.number
                  acctName == account.name
                  distCenter == store.number
                  glAmount == invDistAmountForFirstInvoice
                  bankNumber == bank.number
               }
            }
         }
      }
   }

   void "create one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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
         new VendorDTO(vendorIn),
         new PurchaseOrderDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new VendorDTO(payToIn),
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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
         new VendorDTO(vendorIn),
         new PurchaseOrderDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new VendorDTO(payToIn),
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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
         new VendorDTO(vendorIn),
         new PurchaseOrderDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new VendorDTO(payToIn),
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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
         new VendorDTO(vendorIn),
         new PurchaseOrderDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new VendorDTO(payToIn),
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
      'purchaseOrder'   | new SimpleIdentifiableDTO(UUID.fromString('905545bf-3509-4ad3-8ccc-e437b2dbdcb0')) || 'purchaseOrder.id' | 'system.not.found'                                | "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      'selected'        | new AccountPayableInvoiceSelectedTypeDTO('Z', 'Invalid DTO')                       || 'selected.value'   | 'system.not.found'                                | "Z was unable to be found"
      'type'            | new AccountPayableInvoiceTypeDTO('Z', 'Invalid DTO')                               || 'type.value'       | 'system.not.found'                                | "Z was unable to be found"
      'status'          | new AccountPayableInvoiceStatusTypeDTO('Z', 'Invalid DTO')                         || 'status.value'     | 'system.not.found'                                | "Z was unable to be found"
      'location'        | new SimpleLegacyIdentifiableDTO(0)                                                 || 'location.id'      | 'system.not.found'                                | "0 was unable to be found"
      'discountPercent' | -0.1212345                                                                         || 'discountPercent'  | 'javax.validation.constraints.DecimalMin.message' | 'must be greater than or equal to value'
      'discountPercent' | 0.12123456                                                                         || 'discountPercent'  | 'javax.validation.constraints.Digits.message'     | '0.12123456 is out of range for discountPercent'
      'discountPercent' | 1                                                                                  || 'discountPercent'  | 'javax.validation.constraints.DecimalMax.message' | 'must be less than or equal to value'
   }

   void "update one" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)
      final updatedAPInvoice = dataLoaderService.singleDTO(
         company,
         new VendorDTO(vendorIn),
         new PurchaseOrderDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new VendorDTO(payToIn),
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)
      final updatedAPInvoice = dataLoaderService.singleDTO(
         company,
         new VendorDTO(vendorIn),
         new PurchaseOrderDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new VendorDTO(payToIn),
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)
      final updatedAPInvoice = dataLoaderService.singleDTO(
         company,
         new VendorDTO(vendorIn),
         new PurchaseOrderDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new VendorDTO(payToIn),
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
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
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

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)
      final updatedAPInvoice = dataLoaderService.singleDTO(
         company,
         new VendorDTO(vendorIn),
         new PurchaseOrderDTO(purchaseOrderIn),
         new EmployeeValueObject(employeeIn),
         new VendorDTO(payToIn),
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
      'purchaseOrder' | new SimpleIdentifiableDTO(UUID.fromString('905545bf-3509-4ad3-8ccc-e437b2dbdcb0')) || 'purchaseOrder.id' | "905545bf-3509-4ad3-8ccc-e437b2dbdcb0 was unable to be found"
      'selected'      | new AccountPayableInvoiceSelectedTypeDTO('Z', 'Invalid DTO')                       || 'selected.value'   | "Z was unable to be found"
      'type'          | new AccountPayableInvoiceTypeDTO('Z', 'Invalid DTO')                               || 'type.value'       | "Z was unable to be found"
      'status'        | new AccountPayableInvoiceStatusTypeDTO('Z', 'Invalid DTO')                         || 'status.value'     | "Z was unable to be found"
      'location'      | new SimpleLegacyIdentifiableDTO(0)                                                 || 'location.id'      | "0 was unable to be found"
   }

   void "fetch check preview" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(1, company) as StoreEntity
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
      final statusO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")

      def apInvoiceEntities = dataLoaderService.stream(20, company, vendorIn, purchaseOrderIn, null, employeeIn, null, statusO, payToIn, store, 0.02)
              .sorted { o1, o2 -> o1.id <=> o2.id }.sorted{
         o1, o2 -> o1.invoice <=> o2.invoice
      }.toList()

      def apInvoices = apInvoiceEntities.stream().map { new AccountPayableInvoiceDTO(it) }.toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def pmtStatuses = AccountPayablePaymentStatusTypeDataLoader.predefined()
      def pmtTypes = AccountPayablePaymentTypeTypeDataLoader.predefined()

      def apPayments = accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }).toList()
      apPayments.addAll(accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'V' }, pmtTypes.find { it.value == 'C' }, null, null, null, true).toList())
      final accountPayableControl = accountPayableControlDataLoaderService.single(company, account, account)


      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntities[0], apPayments[0]).toList()
      apPaymentDetails.addAll(apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntities[0], apPayments[1]).toList())
      apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntities[1], apPayments[2]).toList()
      apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntities[1], apPayments[3]).toList()

      def template = apDistTemplateDataLoaderService.single(company)
      def apDistribution = apDistDetailDataLoaderService.single(store, account, company, template)

      sql.executeUpdate([invoice_id: apInvoices[0].id, account_id: account.id, profit_center_sfk: store.number, amount: 1000], """
         INSERT INTO account_payable_invoice_distribution(invoice_id, distribution_account_id, distribution_profit_center_id_sfk, distribution_amount)
	      VALUES (:invoice_id, :account_id, :profit_center_sfk, :amount)
         """)
      sql.executeUpdate([invoice_id: apInvoices[1].id, account_id: account.id, profit_center_sfk: store.number, amount: 2000], """
         INSERT INTO account_payable_invoice_distribution(invoice_id, distribution_account_id, distribution_profit_center_id_sfk, distribution_amount)
	      VALUES (:invoice_id, :account_id, :profit_center_sfk, :amount)
         """)

      def filterRequest = new AccountPayableCheckPreviewFilterRequest( "V", "ASC", bank.number, "100", null, null, null, null, null )

      when:
      def result = get("$path/check-preview${filterRequest}")

      then:
      notThrown(Exception)
      with(result) {
         netPaid != null
         gross != null
         vendorList.eachWithIndex { vendor, index ->
            vendor.netPaid != null
            vendor.gross != null
            vendor.vendorNumber == vendorIn.number
            vendor.vendorName == vendorIn.name
            vendor.invoiceList != null
         }
      }
   }

   void "fetch vendor balance" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(1, company) as StoreEntity
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
      final statusO = new AccountPayableInvoiceStatusType(3, "P", "Paid", "paid")

      def apInvoiceEntitiesPriorDate = dataLoaderService.stream(20, company, vendorIn, purchaseOrderIn, 100.00, employeeIn, null, statusO, payToIn, store, 0.02)
         .sorted { o1, o2 -> o1.id <=> o2.id }.sorted{
         o1, o2 -> o1.invoice <=> o2.invoice
      }.toList()

      def apInvoiceEntitiesCriteriaDate = dataLoaderService.stream(20, company, vendorIn, purchaseOrderIn, 200.00, employeeIn, null, statusO, payToIn, store, 0.02, LocalDate.now().plusMonths(1))
         .sorted { o1, o2 -> o1.id <=> o2.id }.sorted{
         o1, o2 -> o1.invoice <=> o2.invoice
      }.toList()

      def apInvoices = apInvoiceEntitiesPriorDate.stream().map { new AccountPayableInvoiceDTO(it) }.toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def pmtStatuses = AccountPayablePaymentStatusTypeDataLoader.predefined()
      def pmtTypes = AccountPayablePaymentTypeTypeDataLoader.predefined()

      def apPayments = accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }, LocalDate.now().plusMonths(1)).toList()
      apPayments.addAll(accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }, LocalDate.now().plusMonths(1)).toList())
      final accountPayableControl = accountPayableControlDataLoaderService.single(company, account, account)


      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntitiesPriorDate[0], apPayments[0], 200, 0.00).toList()
      apPaymentDetails.addAll(apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntitiesPriorDate[0], apPayments[1], 200, 0.00).toList())
      apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntitiesPriorDate[1], apPayments[2], 200, 0.00).toList()
      apPaymentDetailDataLoaderService.stream(5, company, vendorIn, apInvoiceEntitiesPriorDate[1], apPayments[3], 200, 0.00).toList()

      def template = apDistTemplateDataLoaderService.single(company)
      def apDistribution = apDistDetailDataLoaderService.single(store, account, company, template)

      sql.executeUpdate([invoice_id: apInvoices[0].id, account_id: account.id, profit_center_sfk: store.number, amount: 1000], """
         INSERT INTO account_payable_invoice_distribution(invoice_id, distribution_account_id, distribution_profit_center_id_sfk, distribution_amount)
	      VALUES (:invoice_id, :account_id, :profit_center_sfk, :amount)
         """)
      sql.executeUpdate([invoice_id: apInvoices[1].id, account_id: account.id, profit_center_sfk: store.number, amount: 2000], """
         INSERT INTO account_payable_invoice_distribution(invoice_id, distribution_account_id, distribution_profit_center_id_sfk, distribution_amount)
	      VALUES (:invoice_id, :account_id, :profit_center_sfk, :amount)
         """)

      def filterRequest = new AccountPayableVendorBalanceReportFilterRequest(payToIn.number, payToIn.number, YearMonth.now().plusMonths(1).atDay(1), YearMonth.now().plusMonths(1).atEndOfMonth(), "V")

      when:
      def result = get("$path//vendor-balance${filterRequest}")

      then:
      notThrown(Exception)
      result[0].balance == 2000.00
      result[0].number == payToIn.number
      result[0].invoiceList.size() == 22
      result[0].invoiceList.last().balance == 5600.00
   }

   void "ap invoice maintenance" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(1, company) as StoreEntity
      final vendorPmtTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final poVendorPmtTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final poPmtTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final payToPmtTerm = vendorPaymentTermTestDataLoaderService.singleWithTwoMonthPayments(company)
      final shipViaList = shipViaFactoryService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()

      final vendorShipVia = shipViaList[0]
      final vendorIn = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)


      final poVendorShipVia = shipViaList[1]
      final poVendor = vendorTestDataLoaderService.single(company, poVendorPmtTerm, poVendorShipVia)
      final poApprovedBy = employeeList[0]
      final poPurchaseAgent = employeeList[1]
      final poShipVia = shipViaList[2]
      final poVendorSubEmp = employeeList[2]
      final purchaseOrderIn = purchaseOrderDataLoaderService.single(company, poVendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)

      final employeeIn = employeeList[3]

      final payToShipVia = shipViaList[3]
      final payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      final statusO = new AccountPayableInvoiceStatusType(3, "P", "Paid", "paid")

      final defProfitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      final defAPAcct = accountFactoryService.single(company)
      final defAPDiscAcct = accountFactoryService.single(company)
      final defARAcct = accountFactoryService.single(company)
      final defARDiscAcct = accountFactoryService.single(company)
      final defAcctMiscInvAcct = accountFactoryService.single(company)
      final defAcctSerializedInvAcct = accountFactoryService.single(company)
      final defAcctUnbilledInvAcct = accountFactoryService.single(company)
      final defAcctFreightAcct = accountFactoryService.single(company)
      final generalLedgerControl = generalLedgerControlDataLoaderService.single(
         company,
         defProfitCenter,
         defAPAcct,
         defAPDiscAcct,
         defARAcct,
         defARDiscAcct,
         defAcctMiscInvAcct,
         defAcctSerializedInvAcct,
         defAcctUnbilledInvAcct,
         defAcctFreightAcct
      )

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def pmtStatuses = AccountPayablePaymentStatusTypeDataLoader.predefined()
      def pmtTypes = AccountPayablePaymentTypeTypeDataLoader.predefined()

      def apPayments = accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }, LocalDate.now().plusMonths(1)).toList()
      apPayments.addAll(accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }, LocalDate.now().plusMonths(1)).toList())
      final accountPayableControl = accountPayableControlDataLoaderService.single(company, account, account)

      final existingAPInvoice = dataLoaderService.single(company, vendorIn, purchaseOrderIn, null, employeeIn, null, statusO, payToIn, store, null)
      def existingAPInvoiceDTO = new AccountPayableInvoiceDTO(existingAPInvoice)

      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendorIn, existingAPInvoice, apPayments[0], 200, 0.00).toList()
      apPaymentDetails.addAll(apPaymentDetailDataLoaderService.stream(5, company, vendorIn, existingAPInvoice, apPayments[1], 200, 0.00).toList())

      def template = apDistTemplateDataLoaderService.single(company)
      def apDistribution = apDistDetailDataLoaderService.single(store, account, company, template)
      def templateDTO = new AccountPayableDistributionTemplateDTO(template)
      def apPaymentDTO = new AccountPayablePaymentDTO(apPayments[0])
      def invMaintDTO = new AccountPayableInvoiceMaintenanceDTO(existingAPInvoiceDTO, apPaymentDTO, templateDTO, null)

      when:
      def result = post("$path/maintenance", invMaintDTO)

      then:
      notThrown(Exception)
      result.apInvoiceSchedule != null
   }
}
