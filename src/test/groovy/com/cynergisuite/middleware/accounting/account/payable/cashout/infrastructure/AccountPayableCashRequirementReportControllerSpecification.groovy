package com.cynergisuite.middleware.accounting.account.payable.cashout.infrastructure

import com.cynergisuite.domain.CashRequirementFilterRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.LocalDate

@MicronautTest(transactional = false)
class AccountPayableCashRequirementReportControllerSpecification extends ControllerSpecificationBase {
   private static String path = "/accounting/account-payable/cashout"

   @Inject AccountTestDataLoaderService accountTestDataLoaderService
   @Inject AccountPayableInvoiceDataLoaderService apInvoiceDataLoaderService
   @Inject AccountPayablePaymentDataLoaderService apPaymentDataLoaderService
   @Inject AccountPayablePaymentDetailDataLoaderService apPaymentDetailDataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject PurchaseOrderTestDataLoaderService poTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject AccountPayableInvoiceRepository accountPayableInvoiceRepository

   void "fetch all with multiple vendors and invoices"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def shipViaList = shipViaTestDataLoaderService.stream(4, company).toList()

      def vendor1PmtTerm = vendorPaymentTermList[0]
      def vendor1ShipVia = shipViaList[0]
      def vendor1 = vendorTestDataLoaderService.single(company, vendor1PmtTerm, vendor1ShipVia)
      def vendor2PmtTerm = vendorPaymentTermList[1]
      def vendor2ShipVia = shipViaList[1]
      def vendor2 = vendorTestDataLoaderService.single(company, vendor2PmtTerm, vendor2ShipVia)
      def vendor3 = vendorTestDataLoaderService.single(company, vendor1PmtTerm, vendor2ShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn1 = poTestDataLoaderService.single(company, vendor1, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn2 = poTestDataLoaderService.single(company, vendor2, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn3 = poTestDataLoaderService.single(company, vendor3, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def statusTypeO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")
      def apInvoicesForVend1 = []
      for(int i = 0; i < 4; i++) {
         apInvoicesForVend1.add(apInvoiceDataLoaderService.single(company, vendor1, purchaseOrderIn1, null, employeeIn, null, statusTypeO, payToIn, store))
      }
      def apInvoicesForVend2 = []
      for(int i = 0; i < 4; i++) {
         apInvoicesForVend2.add(apInvoiceDataLoaderService.single(company, vendor2, purchaseOrderIn2, null, employeeIn, null, statusTypeO, payToIn, store))
      }
      def apInvoicesForVend3 = []
      for(int i = 0; i < 4; i++) {
         apInvoicesForVend3.add(apInvoiceDataLoaderService.single(company, vendor3, purchaseOrderIn3, null, employeeIn, null, statusTypeO, payToIn, store))
      }

      def account = accountTestDataLoaderService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def apPayment1 = apPaymentDataLoaderService.single(company, bank, vendor1)
      def apPayment2 = apPaymentDataLoaderService.single(company, bank, vendor2)
      def apPayment3 = apPaymentDataLoaderService.single(company, bank, vendor3)
      apInvoicesForVend1.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor1, it, apPayment1)
      }
      apInvoicesForVend2.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor2, it, apPayment2)
      }
      apInvoicesForVend3.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor3, it, apPayment3)
      }

      def currentDate = LocalDate.now()

      def filterRequest = new CashRequirementFilterRequest([sortBy: "number", sortDirection: "ASC"])
      switch (criteria) {
         case 'Vendors by vendorStart':
            filterRequest['beginVendor'] = vendor1.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by vendorEnd':
            filterRequest['endVendor'] = vendor2.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by vendorStart and vendorEnd':
            filterRequest['beginVendor'] = vendor2.number
            filterRequest['endVendor'] = vendor3.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
      }

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.vendors.size() == vendorCount
      where:
      criteria                                     || vendorCount
      'Vendors by vendorStart'                     || 3
      'Vendors by vendorEnd'                       || 2
      'Vendors by vendorStart and vendorEnd'       || 2
   }

   void "fetch all with many vendors"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPmtTermIn = vendorPaymentTermTestDataLoaderService.single(company)
      def shipViaIn = shipViaTestDataLoaderService.single(company)
      def employeeIn = employeeFactoryService.single(company)
      def payToIn = vendorTestDataLoaderService.single(company, vendorPmtTermIn, shipViaIn)
      def statusTypeO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")
      def account = accountTestDataLoaderService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def vendorList = vendorTestDataLoaderService.stream(20, company, vendorPmtTermIn, shipViaIn).toList()

      def purchaseOrderList = []
      vendorList.each {
         purchaseOrderList.add(poTestDataLoaderService.single(
            company,
            it,
            employeeFactoryService.single(company),
            employeeFactoryService.single(company),
            shipViaIn,
            store,
            vendorPaymentTermTestDataLoaderService.single(company),
            employeeFactoryService.single(company)
         ))
      }

      vendorList.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(
            company,
            vendorList[index],
            apInvoiceDataLoaderService.single(
               company,
               vendorList[index],
               purchaseOrderList[index],
               null,
               employeeIn,
               null,
               statusTypeO,
               payToIn,
               store
            ),
            apPaymentDataLoaderService.single(company, bank, vendorList[index])
         )
      }

      def currentDate = LocalDate.now()

      def filterRequest = new CashRequirementFilterRequest([sortBy: "number", sortDirection: "ASC"])
      switch (criteria) {
         case 'Vendors by vendorStart':
            filterRequest['beginVendor'] = vendorList[15].number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by vendorEnd':
            filterRequest['endVendor'] = vendorList[4].number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by vendorStart and vendorEnd':
            filterRequest['beginVendor'] = vendorList[5].number
            filterRequest['endVendor'] = vendorList[14].number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by null vendorStart and vendorEnd':
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
      }

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.vendors.size() == vendorCount
      where:
      criteria                                     || vendorCount
      'Vendors by vendorStart'                     || 5
      'Vendors by vendorEnd'                       || 5
      'Vendors by vendorStart and vendorEnd'       || 10
      'Vendors by null vendorStart and vendorEnd'  || 20
   }

   void "fetch all with multiple vendors and invoices sort by vendor name"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def shipViaList = shipViaTestDataLoaderService.stream(4, company).toList()

      def vendor1PmtTerm = vendorPaymentTermList[0]
      def vendor1ShipVia = shipViaList[0]
      def vendor1 = vendorTestDataLoaderService.single(company, vendor1PmtTerm, vendor1ShipVia)
      def vendor2PmtTerm = vendorPaymentTermList[1]
      def vendor2ShipVia = shipViaList[1]
      def vendor2 = vendorTestDataLoaderService.single(company, vendor2PmtTerm, vendor2ShipVia)
      def vendor3 = vendorTestDataLoaderService.single(company, vendor1PmtTerm, vendor2ShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn1 = poTestDataLoaderService.single(company, vendor1, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn2 = poTestDataLoaderService.single(company, vendor2, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn3 = poTestDataLoaderService.single(company, vendor3, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def statusTypeO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")
      def apInvoicesForVend1 = apInvoiceDataLoaderService.stream(4, company, vendor1, purchaseOrderIn1, null, employeeIn, null, statusTypeO, payToIn, store).toList()
      def apInvoicesForVend2 = apInvoiceDataLoaderService.stream(4, company, vendor2, purchaseOrderIn2, null, employeeIn, null, statusTypeO, payToIn, store).toList()
      def apInvoicesForVend3 = apInvoiceDataLoaderService.stream(4, company, vendor3, purchaseOrderIn3, null, employeeIn, null, statusTypeO, payToIn, store).toList()

      def account = accountTestDataLoaderService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def apPayment1 = apPaymentDataLoaderService.single(company, bank, vendor1)
      def apPayment2 = apPaymentDataLoaderService.single(company, bank, vendor2)
      def apPayment3 = apPaymentDataLoaderService.single(company, bank, vendor3)
      apInvoicesForVend1.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor1, it, apPayment1)
      }
      apInvoicesForVend2.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor2, it, apPayment2)
      }
      apInvoicesForVend3.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor3, it, apPayment3)
      }

      def currentDate = LocalDate.now()

      def filterRequest = new CashRequirementFilterRequest([sortBy: "name", sortDirection: "ASC"])
      switch (criteria) {
         case 'Vendors by vendorStart':
            filterRequest['beginVendor'] = vendor1.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by vendorEnd':
            filterRequest['endVendor'] = vendor2.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by vendorStart and vendorEnd':
            filterRequest['beginVendor'] = vendor2.number
            filterRequest['endVendor'] = vendor3.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by null vendorStart and vendorEnd':
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
      }

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.vendors.size() == vendorCount
      where:
      criteria                                     || vendorCount
      'Vendors by vendorStart'                     || 3
      'Vendors by vendorEnd'                       || 2
      'Vendors by vendorStart and vendorEnd'       || 2
      'Vendors by null vendorStart and vendorEnd'  || 3
   }

   void "fetch all resulting in empty report"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def shipViaList = shipViaTestDataLoaderService.stream(4, company).toList()

      def vendor1PmtTerm = vendorPaymentTermList[0]
      def vendor1ShipVia = shipViaList[0]
      def vendor1 = vendorTestDataLoaderService.single(company, vendor1PmtTerm, vendor1ShipVia)
      def vendor2PmtTerm = vendorPaymentTermList[1]
      def vendor2ShipVia = shipViaList[1]
      def vendor2 = vendorTestDataLoaderService.single(company, vendor2PmtTerm, vendor2ShipVia)
      def vendor3 = vendorTestDataLoaderService.single(company, vendor1PmtTerm, vendor2ShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn1 = poTestDataLoaderService.single(company, vendor1, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn2 = poTestDataLoaderService.single(company, vendor2, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn3 = poTestDataLoaderService.single(company, vendor3, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def statusTypeH = new AccountPayableInvoiceStatusType(4, "D", "Deleted", "deleted")
      def apInvoicesForVend1 = apInvoiceDataLoaderService.stream(3, company, vendor1, purchaseOrderIn1, null, employeeIn, null, statusTypeH, payToIn, store).toList()
      def apInvoicesForVend2 = apInvoiceDataLoaderService.stream(4, company, vendor2, purchaseOrderIn2, null, employeeIn, null, statusTypeH, payToIn, store).toList()
      def apInvoicesForVend3 = apInvoiceDataLoaderService.stream(5, company, vendor3, purchaseOrderIn3, null, employeeIn, null, statusTypeH, payToIn, store).toList()

      def account = accountTestDataLoaderService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def apPayment1 = apPaymentDataLoaderService.single(company, bank, vendor1)
      def apPayment2 = apPaymentDataLoaderService.single(company, bank, vendor2)
      def apPayment3 = apPaymentDataLoaderService.single(company, bank, vendor3)
      apInvoicesForVend1.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor1, it, apPayment1)
      }
      apInvoicesForVend2.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor2, it, apPayment2)
      }
      apInvoicesForVend3.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor3, it, apPayment3)
      }

      def currentDate = LocalDate.now()

      def filterRequest = new CashRequirementFilterRequest([sortBy: "number", sortDirection: "ASC"])
      switch (criteria) {
         case 'Vendors by vendorStart':
            filterRequest['beginVendor'] = vendor1.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by vendorEnd':
            filterRequest['endVendor'] = vendor1.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by vendorStart and vendorEnd':
            filterRequest['beginVendor'] = vendor2.number
            filterRequest['endVendor'] = vendor3.number
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
         case 'Vendors by null vendorStart and vendorEnd':
            filterRequest['fromDateOne'] = currentDate.minusDays(31)
            filterRequest['thruDateOne'] = currentDate.minusDays(28)
            filterRequest['fromDateTwo'] = currentDate.minusDays(27)
            filterRequest['thruDateTwo'] = currentDate.minusDays(21)
            filterRequest['fromDateThree'] = currentDate.minusDays(20)
            filterRequest['thruDateThree'] = currentDate.minusDays(14)
            filterRequest['fromDateFour'] = currentDate.minusDays(13)
            filterRequest['thruDateFour'] = currentDate.minusDays(7)
            filterRequest['fromDateFive'] = currentDate.minusDays(6)
            filterRequest['thruDateFive'] = currentDate
            break
      }

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.vendors.size() == vendorCount
      where:
      criteria                                     || vendorCount
      'Vendors by vendorStart'                     || 0
      'Vendors by vendorEnd'                       || 0
      'Vendors by vendorStart and vendorEnd'       || 0
      'Vendors by null vendorStart and vendorEnd'  || 0
   }

   void "fetch all with multiple vendors and invoices correct calculations"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def shipViaList = shipViaTestDataLoaderService.stream(4, company).toList()

      def vendor1PmtTerm = vendorPaymentTermList[0]
      def vendor1ShipVia = shipViaList[0]
      def vendor1 = vendorTestDataLoaderService.single(company, vendor1PmtTerm, vendor1ShipVia)
      def vendor2PmtTerm = vendorPaymentTermList[1]
      def vendor2ShipVia = shipViaList[1]
      def vendor2 = vendorTestDataLoaderService.single(company, vendor2PmtTerm, vendor2ShipVia)
      def vendor3 = vendorTestDataLoaderService.single(company, vendor1PmtTerm, vendor2ShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn1 = poTestDataLoaderService.single(company, vendor1, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn2 = poTestDataLoaderService.single(company, vendor2, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn3 = poTestDataLoaderService.single(company, vendor3, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def statusTypeO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")
      def apInvoicesForVend1 = []
      for(int i = 0; i < 4; i++) {
         apInvoicesForVend1.add(apInvoiceDataLoaderService.singleDTO(company, new SimpleIdentifiableDTO(vendor1.myId()), new SimpleIdentifiableDTO(purchaseOrderIn1.myId()), new EmployeeValueObject(employeeIn),  new SimpleIdentifiableDTO(payToIn.myId()), new SimpleLegacyIdentifiableDTO(store.myId())))
      }
      def selected = AccountPayableInvoiceSelectedTypeDataLoader.random()
      def invoiceType = AccountPayableInvoiceTypeDataLoader.random()
      def apInvoicesEntity = []
      apInvoicesForVend1.eachWithIndex { it, index ->
         it.invoiceAmount = 100.00
         it.paidAmount = 50 - (index * 10)
         it.discountTaken = 50
         it.dueDate = LocalDate.now().minusWeeks(index)
         apInvoicesEntity.add(accountPayableInvoiceRepository.insert(new AccountPayableInvoiceEntity(it, vendor1, new SimpleIdentifiableEntity(purchaseOrderIn1), employeeIn, selected, invoiceType, statusTypeO, new SimpleIdentifiableEntity(payToIn.myId()), new SimpleLegacyIdentifiableEntity(store.myId()) ), company))
      }

      def apInvoicesForVend2 = []
      for(int i = 0; i < 4; i++) {
         apInvoicesForVend2.add(apInvoiceDataLoaderService.singleDTO(company, new SimpleIdentifiableDTO(vendor2.myId()), new SimpleIdentifiableDTO(purchaseOrderIn2.myId()), new EmployeeValueObject(employeeIn),  new SimpleIdentifiableDTO(payToIn.myId()), new SimpleLegacyIdentifiableDTO(store.myId())))
      }
      def apInvoicesEntity2 = []
      apInvoicesForVend2.eachWithIndex { it, index ->
         it.invoiceAmount = 200.00
         it.paidAmount = 100 - (index * 10)
         it.discountTaken = 50
         it.dueDate = LocalDate.now().minusWeeks(index)
         apInvoicesEntity2.add(accountPayableInvoiceRepository.insert(new AccountPayableInvoiceEntity(it, vendor2, new SimpleIdentifiableEntity(purchaseOrderIn2), employeeIn, selected, invoiceType, statusTypeO, new SimpleIdentifiableEntity(payToIn.myId()), new SimpleLegacyIdentifiableEntity(store.myId()) ), company))
      }

      def account = accountTestDataLoaderService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def apPayment1 = apPaymentDataLoaderService.single(company, bank, vendor1, null, null, LocalDate.now())
      def apPayment2 = apPaymentDataLoaderService.single(company, bank, vendor1, null, null, LocalDate.now().minusWeeks(1))
      def apPayment3 = apPaymentDataLoaderService.single(company, bank, vendor1, null, null, LocalDate.now().minusWeeks(2))
      def apPayment4 = apPaymentDataLoaderService.single(company, bank, vendor1, null, null, LocalDate.now().minusWeeks(3))
      def paymentsArr = [apPayment1, apPayment2, apPayment3, apPayment4]

      apInvoicesEntity.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor1, it, paymentsArr[index])
      }

      apInvoicesEntity2.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor2, it, paymentsArr[index])
      }

      def currentDate = LocalDate.now()

      def filterRequest = new CashRequirementFilterRequest([sortBy: "number", sortDirection: "ASC"])

      filterRequest['beginVendor'] = vendor1.number
      filterRequest['fromDateOne'] = currentDate.minusDays(31)
      filterRequest['thruDateOne'] = currentDate.minusDays(28)
      filterRequest['fromDateTwo'] = currentDate.minusDays(27)
      filterRequest['thruDateTwo'] = currentDate.minusDays(21)
      filterRequest['fromDateThree'] = currentDate.minusDays(20)
      filterRequest['thruDateThree'] = currentDate.minusDays(14)
      filterRequest['fromDateFour'] = currentDate.minusDays(13)
      filterRequest['thruDateFour'] = currentDate.minusDays(7)
      filterRequest['fromDateFive'] = currentDate.minusDays(6)
      filterRequest['thruDateFive'] = currentDate

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.vendors[0].vendorTotals.weekOneDue  == 0.00
      result.vendors[0].vendorTotals.weekOnePaid == 0.00
      result.vendors[0].vendorTotals.weekFiveDue == 0.00
      result.vendors[0].vendorTotals.weekFivePaid == 50.00
      result.vendors[0].vendorTotals.weekThreeDue == 20.00
      result.vendors[0].vendorTotals.weekThreePaid == 30.00
      result.cashoutTotals.weekTwoPaid == 90.00
      result.cashoutTotals.weekFourDue == 70.00
      result.cashoutTotals.weekOnePaid == 0.00
   }
}
