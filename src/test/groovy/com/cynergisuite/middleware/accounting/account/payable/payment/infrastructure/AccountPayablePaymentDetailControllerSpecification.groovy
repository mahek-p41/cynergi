package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AccountPayablePaymentDetailControllerSpecification extends ControllerSpecificationBase {
   private static String path = "/accounting/account-payable/payment/detail"

   @Inject AccountPayablePaymentDataLoaderService dataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject AccountTestDataLoaderService accountFactoryService
   @Inject PurchaseOrderTestDataLoaderService poDataLoaderService
   @Inject AccountPayablePaymentDetailDataLoaderService apPaymentDetailDataLoaderService
   @Inject AccountPayableInvoiceDataLoaderService payableInvoiceDataLoaderService

   void "fetch one"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendor = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendor)
      def apPaymentDetail = apPaymentDetailDataLoaderService.single(company, vendor, apInvoice, apPayment)

      when:
      def result = get("$path/${apPaymentDetail.id}")

      then:
      notThrown(Exception)
      with(result) {
         id == apPaymentDetail.id
         vendor.id == apPaymentDetail.vendor.id
         with(invoice) {
            id == apPaymentDetail.invoice.id
            invoice == apPaymentDetail.invoice.invoice
            invoiceDate == apPaymentDetail.invoice.invoiceDate.toString()
         }
         payment == null
         invoiceAmount == apPaymentDetail.amount
         discountAmount == apPaymentDetail.discount
      }
   }

   void "fetch one not found"() {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("$path/$nonExistentId")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "create one"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendor = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendor)

      def apPaymentDetail = apPaymentDetailDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.id), new AccountPayableInvoiceDTO(apInvoice), new SimpleIdentifiableDTO(apPayment.id))

      when:
      def result = post("$path", apPaymentDetail)

      then:
      notThrown(Exception)
      with(result) {
         id instanceof UUID
         vendor.id == apPaymentDetail.vendor.id
         with(invoice) {
            id == apPaymentDetail.invoice.id
            invoice == apPaymentDetail.invoice.invoice
            invoiceDate == apPaymentDetail.invoice.invoiceDate.toString()
         }
         payment == null
         invoiceAmount == apPaymentDetail.invoiceAmount
         discountAmount == apPaymentDetail.discountAmount
      }
   }

   void "create valid account payable payment detail without nullable properties"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendorEntity = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendorEntity, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendorEntity, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendorEntity)

      def apPaymentDetail = apPaymentDetailDataLoaderService.singleDTO(null, new AccountPayableInvoiceDTO(apInvoice), new SimpleIdentifiableDTO(apPayment.id))
      apPaymentDetail.discountAmount = null

      when:
      def result = post("$path", apPaymentDetail)

      then:
      notThrown(Exception)
      with(result) {
         id instanceof UUID
         vendor == null
         with(invoice) {
            id == apPaymentDetail.invoice.id
            invoice == apPaymentDetail.invoice.invoice
            invoiceDate == apPaymentDetail.invoice.invoiceDate.toString()
         }
         payment == null
         invoiceAmount == apPaymentDetail.invoiceAmount
         discountAmount == null
      }
   }

   @Unroll
   void "create invalid account payable payment detail without #nonNullableProp"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendor = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendor)

      def apPaymentDetail = apPaymentDetailDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.id), new AccountPayableInvoiceDTO(apInvoice), new SimpleIdentifiableDTO(apPayment.id))
      apPaymentDetail["$nonNullableProp"] = null

      when:
      post("$path", apPaymentDetail)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'
      response[0].code == 'javax.validation.constraints.NotNull.message'

      where:
      nonNullableProp || errorResponsePath
      'invoice'       || 'invoice'
      'payment'       || 'payment'
      'invoiceAmount' || 'invoiceAmount'
   }

   @Unroll
   void "create invalid account payable payment detail with non-existent #testProp"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendor = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendor)

      def apPaymentDetail = apPaymentDetailDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendor.id), new AccountPayableInvoiceDTO(apInvoice), new SimpleIdentifiableDTO(apPayment.id))
      apPaymentDetail["$testProp"] = invalidValue

      when:
      def result = post("$path", apPaymentDetail)
      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage
      response[0].code == 'system.not.found'

      where:
      testProp  | invalidValue                                                                             || errorResponsePath | errorMessage
      'vendor'  | new SimpleIdentifiableDTO([id: UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702')]) || 'vendor.id'       | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
      'payment' | new SimpleIdentifiableDTO(UUID.fromString('ee2359b6-c88c-11eb-8098-02420a4d0702'))       || 'payment.id'      | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
   }

   void "update one"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendors = vendorTestDataLoaderService.stream(2, company, vendorPmtTerm, vendorShipVia).toList()

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendors[0], poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoices = payableInvoiceDataLoaderService.stream(2, company, vendors[0], purchaseOrderIn, null, employeeIn, null, null, payToIn, store).toList()

      def apPayments = dataLoaderService.stream(2, company, bank, vendors[0]).toList()

      def existingPaymentDetail = apPaymentDetailDataLoaderService.single(company, vendors[0], apInvoices[0], apPayments[0])
      def updatedPaymentDetail = apPaymentDetailDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendors[1].id), new AccountPayableInvoiceDTO(apInvoices[1]), new SimpleIdentifiableDTO(apPayments[1].id))
      updatedPaymentDetail.id = existingPaymentDetail.id

      when:
      def result = put("$path/${existingPaymentDetail.id}", updatedPaymentDetail)

      then:
      notThrown(Exception)
      with(result) {
         id instanceof UUID
         vendor.id == updatedPaymentDetail.vendor.id
         invoice.id == updatedPaymentDetail.invoice.id
         payment == null
         invoiceAmount == updatedPaymentDetail.invoiceAmount
         discountAmount == updatedPaymentDetail.discountAmount
      }
   }

   void "update valid account payable payment detail without nullable properties"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendors = vendorTestDataLoaderService.stream(2, company, vendorPmtTerm, vendorShipVia).toList()

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendors[0], poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoices = payableInvoiceDataLoaderService.stream(2, company, vendors[0], purchaseOrderIn, null, employeeIn, null, null, payToIn, store).toList()

      def apPayments = dataLoaderService.stream(2, company, bank, vendors[0]).toList()

      def existingPaymentDetail = apPaymentDetailDataLoaderService.single(company, vendors[0], apInvoices[0], apPayments[0])
      def updatedPaymentDetail = apPaymentDetailDataLoaderService.singleDTO(null, new AccountPayableInvoiceDTO(apInvoices[1]), new SimpleIdentifiableDTO(apPayments[1].id))
      updatedPaymentDetail.id = existingPaymentDetail.id
      updatedPaymentDetail.discountAmount = null

      when:
      def result = put("$path/${existingPaymentDetail.id}", updatedPaymentDetail)

      then:
      notThrown(Exception)
      with(result) {
         id instanceof UUID
         vendor == null
         with(invoice) {
            id == updatedPaymentDetail.invoice.id
            invoice == updatedPaymentDetail.invoice.invoice
            invoiceDate == updatedPaymentDetail.invoice.invoiceDate.toString()
         }
         payment == null
         invoiceAmount == updatedPaymentDetail.invoiceAmount
         discountAmount == null
      }
   }

   @Unroll
   void "update invalid account payable payment detail without #nonNullableProp"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendors = vendorTestDataLoaderService.stream(2, company, vendorPmtTerm, vendorShipVia).toList()

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendors[0], poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoices = payableInvoiceDataLoaderService.stream(2, company, vendors[0], purchaseOrderIn, null, employeeIn, null, null, payToIn, store).toList()

      def apPayments = dataLoaderService.stream(2, company, bank, vendors[0]).toList()

      def existingPaymentDetail = apPaymentDetailDataLoaderService.single(company, vendors[0], apInvoices[0], apPayments[0])
      def updatedPaymentDetail = apPaymentDetailDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendors[1].id), new AccountPayableInvoiceDTO(apInvoices[1]), new SimpleIdentifiableDTO(apPayments[1].id))
      updatedPaymentDetail.id = existingPaymentDetail.id
      updatedPaymentDetail["$nonNullableProp"] = null

      when:
      put("$path/${existingPaymentDetail.id}", updatedPaymentDetail)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'
      response[0].code == 'javax.validation.constraints.NotNull.message'

      where:
      nonNullableProp || errorResponsePath
      'invoice'       || 'invoice'
      'payment'       || 'payment'
      'invoiceAmount' || 'invoiceAmount'
   }

   @Unroll
   void "update invalid account payable payment detail with non-existing #testProp"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendors = vendorTestDataLoaderService.stream(2, company, vendorPmtTerm, vendorShipVia).toList()

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendors[0], poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoices = payableInvoiceDataLoaderService.stream(2, company, vendors[0], purchaseOrderIn, null, employeeIn, null, null, payToIn, store).toList()
      def apPayments = dataLoaderService.stream(2, company, bank, vendors[0]).toList()

      def existingPaymentDetail = apPaymentDetailDataLoaderService.single(company, vendors[0], apInvoices[0], apPayments[0])
      def updatedPaymentDetail = apPaymentDetailDataLoaderService.singleDTO(new SimpleIdentifiableDTO(vendors[1].id), new AccountPayableInvoiceDTO(apInvoices[1]), new SimpleIdentifiableDTO(apPayments[1].id))
      updatedPaymentDetail["$testProp"] = invalidValue

      when:
      put("$path/${existingPaymentDetail.id}", updatedPaymentDetail)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp  | invalidValue                                                                             || errorResponsePath | errorMessage
      'vendor'  | new SimpleIdentifiableDTO([id: UUID.fromString("ee2359b6-c88c-11eb-8098-02420a4d0702")]) || 'vendor.id'       | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
      'payment' | new SimpleIdentifiableDTO(UUID.fromString("ee2359b6-c88c-11eb-8098-02420a4d0702"))       || 'payment.id'      | 'ee2359b6-c88c-11eb-8098-02420a4d0702 was unable to be found'
   }

   void "delete one account payable payment detail"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def store = storeFactoryService.store(3, company)
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendor = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendor)

      def apPaymentDetail = apPaymentDetailDataLoaderService.single(company, vendor, apInvoice, apPayment)

      when:
      delete("$path/${apPaymentDetail.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${apPaymentDetail.id}")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$apPaymentDetail.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete account payable payment detail from other company is not allowed"() {
      given:
      def tstds2 = companyFactoryService.forDatasetCode('corrto')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, tstds2).toList()
      def store = storeFactoryService.store(6, tstds2)
      def shipViaList = shipViaFactoryService.stream(4, tstds2).toList()

      def account = accountFactoryService.single(tstds2)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendor = vendorTestDataLoaderService.single(tstds2, vendorPmtTerm, vendorShipVia)

      def employeeList = employeeFactoryService.stream(4, tstds2).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poDataLoaderService.single(tstds2, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(tstds2, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(tstds2, vendor, purchaseOrderIn, null, employeeIn, null, null, payToIn, store)

      def apPayment = dataLoaderService.single(tstds2, bank, vendor)
      def apPaymentDetail = apPaymentDetailDataLoaderService.single(tstds2, vendor, apInvoice, apPayment)

      when:
      delete("$path/${apPaymentDetail.id}")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$apPaymentDetail.id was unable to be found"
      response.code == 'system.not.found'
   }
}

