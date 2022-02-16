package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.domain.PaymentReportFilterRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDataLoader
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.group.VendorGroupTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Unroll

import jakarta.inject.Inject
import java.time.OffsetDateTime

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.CONFLICT
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class AccountPayablePaymentControllerSpecification extends ControllerSpecificationBase {
   private static String path = "/accounting/account-payable/payment"

   @Inject AccountPayablePaymentDataLoaderService dataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject AccountTestDataLoaderService accountFactoryService
   @Inject PurchaseOrderTestDataLoaderService poDataLoaderService
   @Inject AccountPayablePaymentDetailDataLoaderService apPaymentDetailDataLoaderService
   @Inject AccountPayableInvoiceDataLoaderService payableInvoiceDataLoaderService
   @Inject VendorGroupTestDataLoaderService vendorGroupTestDataLoaderService

   void "fetch one"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, employeeIn, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendor)
      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendor, apInvoice, apPayment).toList()

      when:
      def result = get("$path/${apPayment.id}")

      then:
      notThrown(Exception)
      with(result) {
         id == apPayment.id
         vendor.id == apPayment.vendor.id
         bank.id == apPayment.bank.id
         with(status) {
            value == apPayment.status.value
            description == apPayment.status.description
         }
         with(type) {
            value == apPayment.type.value
            description == apPayment.type.description
         }
         paymentDate == apPayment.paymentDate.toString()
         dateCleared == apPayment.dateCleared?.toString()
         dateVoided == apPayment.dateVoided?.toString()
         paymentNumber == apPayment.paymentNumber
         amount == apPayment.amount
         paymentDetails.size() == 5

         paymentDetails.eachWithIndex { detail, i ->
            with(detail) {
               id == apPaymentDetails[i].id
               vendor.id == apPaymentDetails[i].vendor.id
               invoice.id == apPaymentDetails[i].invoice.id
               invoiceAmount == apPaymentDetails[i].amount
               discountAmount == apPaymentDetails[i].discount
            }
         }
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

   void "fetch all"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, employeeIn, payToIn, store)
      def apPayments = dataLoaderService.stream(2, company, bank, vendor).toList()
      def reportPaidTotal = Optional.ofNullable(apPayments.findAll { it.status.value == 'P' }.sum { it.amount }).orElse(0)
      def reportVoidTotal = Optional.ofNullable(apPayments.findAll { it.status.value == 'V' }.sum { it.amount }).orElse(0)

      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendor, apInvoice, apPayments[0]).toList()
      apPaymentDetails.addAll(apPaymentDetailDataLoaderService.stream(5, company, vendor, apInvoice, apPayments[1]).toList())

      when:
      def response = get("$path")

      then:
      notThrown(Exception)
      response != null
      response.paidTotal == reportPaidTotal
      response.voidTotal == reportVoidTotal
      response.payments.eachWithIndex { responsePayment, i ->
         with(responsePayment) {
            id == apPayments[i].id
            with(vendor) {
               id == apPayments[i].vendor.id
               name == apPayments[i].vendor.name
               emailAddress == apPayments[i].vendor.emailAddress
            }
            bank.id == apPayments[i].bank.id
            with(status) {
               value == apPayments[i].status.value
               description == apPayments[i].status.description
            }
            with(type) {
               value == apPayments[i].type.value
               description == apPayments[i].type.description
            }
            paymentDate == apPayments[i].paymentDate.toString()
            dateCleared == apPayments[i].dateCleared?.toString()
            dateVoided == apPayments[i].dateVoided?.toString()
            paymentNumber == apPayments[i].paymentNumber
            amount == apPayments[i].amount
            paymentDetails.size() == 5

            paymentDetails.eachWithIndex { detail, k ->
               with(detail) {
                  id == apPaymentDetails[k + i * 5].id
                  vendorNumber == apPaymentDetails[k + i * 5].vendor.number
                  invoice == apPaymentDetails[k + i * 5].invoice.invoice
                  invoiceAmount == apPaymentDetails[k + i * 5].amount
                  invoiceDate == apPaymentDetails[k + i * 5].invoice.invoiceDate.toString()
               }
            }
         }
      }
   }

   @Unroll
   void "filter for report #criteria"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def shipViaList = shipViaFactoryService.stream(4, company).toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendorGroups = vendorGroupTestDataLoaderService.stream(company).toList()
      def vendors = vendorTestDataLoaderService.stream(2, company, vendorPmtTerm, vendorShipVia, vendorGroups[0]).toList()

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendors[0], poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendors[0], purchaseOrderIn, employeeIn, payToIn, store)
      def pmtStatuses = AccountPayablePaymentStatusTypeDataLoader.predefined()
      def pmtTypes = AccountPayablePaymentTypeTypeDataLoader.predefined()
      def apPayments = dataLoaderService.stream(2, company, bank, vendors[0], pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }).toList()
      apPayments.addAll(dataLoaderService.stream(2, company, bank, vendors[1], pmtStatuses.find { it.value == 'V' }, pmtTypes.find { it.value == 'C' }, true).toList())


      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendors[0], apInvoice, apPayments[0]).toList()
      apPaymentDetails.addAll(apPaymentDetailDataLoaderService.stream(5, company, vendors[0], apInvoice, apPayments[1]).toList())
      apPaymentDetails.addAll(apPaymentDetailDataLoaderService.stream(5, company, vendors[1], apInvoice, apPayments[2]).toList())
      apPaymentDetails.addAll(apPaymentDetailDataLoaderService.stream(5, company, vendors[1], apInvoice, apPayments[3]).toList())

      def frmPmtDt = OffsetDateTime.now().minusDays(30)
      def thruPmtDt = OffsetDateTime.now().minusDays(20)

      def frmDtClr = OffsetDateTime.now().minusDays(20)
      def thruDtClr = OffsetDateTime.now().minusDays(15)

      def frmDtVoid = OffsetDateTime.now().minusDays(15)
      def thruDtVoid = OffsetDateTime.now().minusDays(10)

      def filterRequest = new PaymentReportFilterRequest([sortBy: "id", sortDirection: "ASC"])
      switch (criteria) {
         case 'PmtNumberCase1':
            filterRequest['pmtNums'] = [apPayments[0].paymentNumber, apPayments[1].paymentNumber]
            break
         case 'PmtNumberCase2':
            filterRequest['pmtNums'] = [UUID.randomUUID()]
            break
         case 'PmtNumberCase3':
            filterRequest['beginPmt'] = apPayments[0].paymentNumber
            filterRequest['endPmt'] = apPayments[0].paymentNumber
            break
         case 'BankCase1':
            filterRequest['banks'] = [bank.id]
            break
         case 'BankCase2':
            filterRequest['banks'] = [UUID.randomUUID()]
            break
         case 'BankCase3':
            filterRequest['beginBank'] = bank.number
            filterRequest['endBank'] = bank.number + 100
            break
         case 'BankCase4':
            filterRequest['beginBank'] = 0
            filterRequest['endBank'] = 0
            break
         case 'VendorCase1':
            filterRequest['vendors'] = [vendors[0].id]
            break
         case 'VendorCase2':
            filterRequest['vendors'] = [UUID.randomUUID()]
            break
         case 'VendorCase3':
            filterRequest['beginVendor'] = vendors[1].number
            filterRequest['endVendor'] = vendors[1].number + 100
            break
         case 'VendorCase4':
            filterRequest['beginVendor'] = 0
            filterRequest['endVendor'] = 0
            break
         case 'VendorGroupCase1':
            filterRequest['beginVendorGroup'] = vendorGroups[0].value
            filterRequest['endVendorGroup'] = vendorGroups[1].value
            break
         case 'VendorGroupCase2':
            filterRequest['beginVendorGroup'] = 'non-exist-value-1'
            filterRequest['endVendorGroup'] = 'non-exist-value-2'
            break
         case 'StatusCase1':
            filterRequest['status'] = 'P'
            break
         case 'StatusCase2':
            filterRequest['status'] = 'V'
            break
         case 'TypeCase1':
            filterRequest['type'] = 'A'
            break
         case 'TypeCase2':
            filterRequest['type'] = 'C'
            break
         case 'PmtDateCase1':
            filterRequest["frmPmtDt"] = frmPmtDt
            filterRequest["thruPmtDt"] = thruPmtDt
            break
         case 'PmtDateCase2':
            filterRequest["frmPmtDt"] = OffsetDateTime.now().minusDays(20)
            filterRequest["thruPmtDt"] = OffsetDateTime.now().plusDays(1)
            break
         case 'PmtClearedCase1':
            filterRequest["frmDtClr"] = frmDtClr
            break
         case 'PmtClearedCase2':
            filterRequest["frmDtClr"] = OffsetDateTime.now().minusDays(15)
            filterRequest["thruDtClr"] = OffsetDateTime.now().plusDays(1)
            break
         case 'PmtVoidedCase1':
            filterRequest["thruDtVoid"] = thruDtVoid
            break
         case 'PmtVoidedCase2':
            filterRequest["frmDtVoid"] = OffsetDateTime.now().minusDays(10)
            filterRequest["thruDtVoid"] = OffsetDateTime.now().plusDays(1)
            break
         case 'IncludeOption1':
            filterRequest["includeOption"] = 'C'
            break
         case 'IncludeOption2':
            filterRequest["includeOption"] = 'O'
            break
      }

      when:
      def response = get("$path$filterRequest")

      then:
      notThrown(Exception)
      response != null
      response.payments.size() == paymentCount

      where:
      criteria          || paymentCount
      'PmtNumberCase1'  || 2
      'PmtNumberCase2'  || 0
      'PmtNumberCase3'  || 1
      'BankCase1'       || 4
      'BankCase2'       || 0
      'BankCase3'       || 4
      'BankCase4'       || 0
      'VendorCase1'     || 2
      'VendorCase2'     || 0
      'VendorCase3'     || 2
      'VendorCase4'     || 0
      'VendorGroupCase1'|| 4
      'VendorGroupCase2'|| 0
      'StatusCase1'     || 2
      'StatusCase2'     || 2
      'TypeCase1'       || 2
      'TypeCase2'       || 2
      'PmtDateCase1'    || 0
      'PmtDateCase2'    || 4
      'PmtClearedCase1' || 2
      'PmtClearedCase2' || 2
      'PmtVoidedCase1'  || 0
      'PmtVoidedCase2'  || 4
      'IncludeOption1'  || 2
      'IncludeOption2'  || 2
   }

   void "create one"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def apPayment = dataLoaderService.singleDTO(bank, vendor)

      when:
      def result = post("$path", apPayment)

      then:
      notThrown(Exception)
      with(result) {
         id instanceof UUID
         with(vendor) {
            id == apPayment.vendor.id
            name == apPayment.vendor.name
            emailAddress == apPayment.vendor.emailAddress
         }
         bank.id == apPayment.bank.id
         with(status) {
            value == apPayment.status.value
            description == apPayment.status.description
         }
         with(type) {
            value == apPayment.type.value
            description == apPayment.type.description
         }
         paymentDate == apPayment.paymentDate.toString()
         dateCleared == apPayment.dateCleared.toString()
         dateVoided == apPayment.dateVoided.toString()
         paymentNumber == apPayment.paymentNumber
         amount == apPayment.amount
      }
   }

   void "create valid account payable payment without nullable properties"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def apPayment = dataLoaderService.singleDTO(bank, vendor)
      apPayment.dateCleared = null
      apPayment.dateVoided = null

      when:
      def result = post("$path", apPayment)

      then:
      notThrown(Exception)
      with(result) {
         id instanceof UUID
         with(vendor) {
            id == apPayment.vendor.id
            name == apPayment.vendor.name
            emailAddress == apPayment.vendor.emailAddress
         }
         bank.id == apPayment.bank.id
         with(status) {
            value == apPayment.status.value
            description == apPayment.status.description
         }
         with(type) {
            value == apPayment.type.value
            description == apPayment.type.description
         }
         paymentDate == apPayment.paymentDate.toString()
         dateCleared == null
         dateVoided == null
         paymentNumber == apPayment.paymentNumber
         amount == apPayment.amount
      }
   }

   @Unroll
   void "create invalid account payable payment without #nonNullableProp"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def apPayment = dataLoaderService.singleDTO(bank, vendor)
      apPayment["$nonNullableProp"] = null

      when:
      def result = post("$path", apPayment)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == 'Is required'

      where:
      nonNullableProp || errorResponsePath
      'bank'          || 'bank'
      'vendor'        || 'vendor'
      'status'        || 'status'
      'paymentDate'   || 'paymentDate'
      'paymentNumber' || 'paymentNumber'
      'amount'        || 'amount'
   }

   @Unroll
   void "create invalid account payable payment with non-existent #testProp"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def apPayment = dataLoaderService.singleDTO(bank, vendor)
      apPayment["$testProp"] = invalidValue

      when:
      post("$path", apPayment)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp | invalidValue                                                                       || errorResponsePath | errorMessage
      'bank'   | new SimpleIdentifiableDTO(UUID.fromString('048196f1-b848-4698-b10e-4511336612d0')) || 'bank.id'         | "048196f1-b848-4698-b10e-4511336612d0 was unable to be found"
      'status' | new AccountPayablePaymentStatusTypeDTO('Z', 'Invalid DTO')                         || 'status.value'    | 'Z was unable to be found'
      'type'   | new AccountPayablePaymentTypeTypeDTO('Z', 'Invalid DTO')                           || 'type.value'      | 'Z was unable to be found'
   }

   void "update one"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendors[0], poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def existingPayment = dataLoaderService.single(company, bank, vendors[0])
      def updatedPayment = dataLoaderService.singleDTO(bank, vendors[1])
      updatedPayment.id = existingPayment.id

      when:
      def result = put("$path/${existingPayment.id}", updatedPayment)

      then:
      notThrown(Exception)
      with(result) {
         id instanceof UUID
         with(vendor) {
            id == updatedPayment.vendor.id
            name == updatedPayment.vendor.name
            emailAddress == updatedPayment.vendor.emailAddress
         }
         bank.id == updatedPayment.bank.id
         with(status) {
            value == updatedPayment.status.value
            description == updatedPayment.status.description
         }
         with(type) {
            value == updatedPayment.type.value
            description == updatedPayment.type.description
         }
         paymentDate == updatedPayment.paymentDate.toString()
         dateCleared == updatedPayment.dateCleared.toString()
         dateVoided == updatedPayment.dateVoided.toString()
         paymentNumber == updatedPayment.paymentNumber
         amount == updatedPayment.amount
      }
   }

   void "update valid account payable payment without nullable properties"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendors[0], poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)

      def existingPayment = dataLoaderService.single(company, bank, vendors[0])
      def updatedPayment = dataLoaderService.singleDTO(bank, vendors[1])
      updatedPayment.id = existingPayment.id
      updatedPayment.dateCleared = null
      updatedPayment.dateVoided = null

      when:
      def result = put("$path/${existingPayment.id}", updatedPayment)

      then:
      notThrown(Exception)
      with(result) {
         id instanceof UUID
         with(vendor) {
            id == updatedPayment.vendor.id
            name == updatedPayment.vendor.name
            emailAddress == updatedPayment.vendor.emailAddress
         }
         bank.id == updatedPayment.bank.id
         with(status) {
            value == updatedPayment.status.value
            description == updatedPayment.status.description
         }
         with(type) {
            value == updatedPayment.type.value
            description == updatedPayment.type.description
         }
         paymentDate == updatedPayment.paymentDate.toString()
         dateCleared == null
         dateVoided == null
         paymentNumber == updatedPayment.paymentNumber
         amount == updatedPayment.amount
      }
   }

   @Unroll
   void "update invalid account payable payment without #nonNullableProp"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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

      def existingPayment = dataLoaderService.single(company, bank, vendors[0])
      def updatedPayment = dataLoaderService.singleDTO(bank, vendors[1])
      updatedPayment.id = existingPayment.id
      updatedPayment["$nonNullableProp"] = null

      when:
      put("$path/${existingPayment.id}", updatedPayment)

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
      'bank'          || 'bank'
      'vendor'        || 'vendor'
      'status'        || 'status'
      'paymentDate'   || 'paymentDate'
      'paymentNumber' || 'paymentNumber'
      'amount'        || 'amount'
   }

   @Unroll
   void "update invalid account payable payment with non-existent #testProp"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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

      def existingPayment = dataLoaderService.single(company, bank, vendors[0])
      def updatedPayment = dataLoaderService.singleDTO(bank, vendors[1])
      updatedPayment.id = existingPayment.id
      updatedPayment["$testProp"] = invalidValue

      when:
      put("$path/${existingPayment.id}", updatedPayment)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == errorResponsePath
      response[0].message == errorMessage

      where:
      testProp | invalidValue                                                                       || errorResponsePath | errorMessage
      'bank'   | new SimpleIdentifiableDTO(UUID.fromString('048196f1-b848-4698-b10e-4511336612d0')) || 'bank.id'         | "048196f1-b848-4698-b10e-4511336612d0 was unable to be found"
      'status' | new AccountPayablePaymentStatusTypeDTO('Z', 'Invalid DTO')                         || 'status.value'    | 'Z was unable to be found'
      'type'   | new AccountPayablePaymentTypeTypeDTO('Z', 'Invalid DTO')                           || 'type.value'      | 'Z was unable to be found'
   }

   void "delete one account payable payment"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
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
      def poCustAcct = accountFactoryService.single(company)
      def purchaseOrderIn = poDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, employeeIn, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendor)

      when:
      delete("$path/${apPayment.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${apPayment.id}")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$apPayment.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete account payable payment from other company is not allowed"() {
      given:
      def tstds2 = companyFactoryService.forDatasetCode('tstds2')
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, tstds2).toList()
      def store = storeFactoryService.store(3, tstds2)
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
      def poCustAcct = accountFactoryService.single(tstds2)
      def purchaseOrderIn = poDataLoaderService.single(tstds2, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(tstds2, payToPmtTerm, payToShipVia)
      def apInvoice = payableInvoiceDataLoaderService.single(tstds2, vendor, purchaseOrderIn, employeeIn, payToIn, store)

      def apPayment = dataLoaderService.single(tstds2, bank, vendor)

      when:
      delete("$path/${apPayment.id}")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$apPayment.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete account payable payment still has reference"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
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
      def apInvoice = payableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, employeeIn, payToIn, store)

      def apPayment = dataLoaderService.single(company, bank, vendor)

      apPaymentDetailDataLoaderService.stream(5, company, vendor, apInvoice, apPayment).toList()

      when:
      delete("$path/$apPayment.id")

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == CONFLICT
      def response = exception.response.bodyAsJson()
      response.message == "Requested operation violates data integrity"
      response.code == "cynergi.data.constraint.violated"
   }
}
