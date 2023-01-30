package com.cynergisuite.middleware.accounting.account.payable.cashflow.infrastructure


import com.cynergisuite.domain.CashFlowFilterRequest
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
class AccountPayableCashFlowReportControllerSpecification extends ControllerSpecificationBase {
   private static String path = "/accounting/account-payable/cashflow"

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
      def company = companyFactoryService.forDatasetCode('coravt')
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
         apInvoicesForVend1.add(apInvoiceDataLoaderService.single(company, vendor1, purchaseOrderIn1, 100, employeeIn, 100, statusTypeO, payToIn, store))
      }
      def apInvoicesForVend2 = []
      for(int i = 0; i < 4; i++) {
         apInvoicesForVend2.add(apInvoiceDataLoaderService.single(company, vendor2, purchaseOrderIn2, 200, employeeIn, 100, statusTypeO, payToIn, store))      }
      def apInvoicesForVend3 = []
      for(int i = 0; i < 4; i++) {
         apInvoicesForVend3.add(apInvoiceDataLoaderService.single(company, vendor3, purchaseOrderIn3, 500, employeeIn, 100, statusTypeO, payToIn, store))
      }

      def account = accountTestDataLoaderService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def apPayment1 = apPaymentDataLoaderService.single(company, bank, vendor1, null, null, null, null,100)
      def apPayment2 = apPaymentDataLoaderService.single(company, bank, vendor2, null, null, null, null, 100)
      def apPayment3 = apPaymentDataLoaderService.single(company, bank, vendor3, null, null, null, null ,100)
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

      def filterRequest = new CashFlowFilterRequest([sortBy: "number", sortDirection: "ASC"])
      switch (criteria) {
         case 'include details':
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
            filterRequest['details'] = true
            break
         case 'exclude details':
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
            filterRequest['details'] = false
            break
      }

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.vendors.size() == 3
      result.cashflowTotals.dateFiveAmount == 2000
      result.vendors[1].vendorTotals.dateFiveAmount == 400
      result.vendors[1].invoices?.size == invoices
      where:
      criteria                      || invoices
      'exclude details'             || null
      'include details'             || 4
   }

   void "fetch all with multiple vendors and invoices correct calculations"() {
      given:
      def company = companyFactoryService.forDatasetCode('coravt')
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
      def apPayment1 = apPaymentDataLoaderService.single(company, bank, vendor1)
      def apPayment2 = apPaymentDataLoaderService.single(company, bank, vendor2)
      def apPayment3 = apPaymentDataLoaderService.single(company, bank, vendor3)
      apInvoicesEntity.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor1, it, apPayment1)
      }

      apInvoicesEntity2.eachWithIndex { it, index ->
         apPaymentDetailDataLoaderService.single(company, vendor2, it, apPayment1)
      }

      def currentDate = LocalDate.now()

      def filterRequest = new CashFlowFilterRequest([sortBy: "number", sortDirection: "ASC"])

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
      result.vendors[0].vendorTotals.dateOneAmount  == 0.00
      result.vendors[0].vendorTotals.dateTwoAmount == 80.00
      result.vendors[0].vendorTotals.dateThreeAmount == 70.00
      result.vendors[0].vendorTotals.dateFourAmount == 60.00
      result.vendors[0].vendorTotals.dateFiveAmount == 50.00
   }
}
