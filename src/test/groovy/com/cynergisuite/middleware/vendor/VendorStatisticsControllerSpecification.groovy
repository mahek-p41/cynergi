package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.VendorStatisticsFilterRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDataLoader
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarGLAPDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.rebate.RebateTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.LocalDate

@MicronautTest(transactional = false)
class VendorStatisticsControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor-statistics"

   @Inject AccountTestDataLoaderService accountFactoryService
   @Inject AccountPayableInvoiceDataLoaderService accountPayableInvoiceDataLoaderService
   @Inject AccountPayablePaymentDataLoaderService accountPayablePaymentDataLoaderService
   @Inject AccountPayablePaymentDetailDataLoaderService apPaymentDetailDataLoaderService
   @Inject AccountTestDataLoaderService accountTestDataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject FinancialCalendarDataLoaderService financialCalendarDataLoaderService
   @Inject PurchaseOrderTestDataLoaderService purchaseOrderDataLoaderService
   @Inject RebateTestDataLoaderService rebateTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService

   void "fetch statistics with unpaid balance" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]

      def payToPmtTerm = vendorPaymentTermList[3]

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, shipVia)

      def purchaseOrderIn = purchaseOrderDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, shipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def openAccountPayableInvoiceStatus = AccountPayableInvoiceStatusTypeDataLoader.predefined().get(0)

      def apInvoice = accountPayableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, 1000.00, employeeIn, 500.00, openAccountPayableInvoiceStatus, payToIn, store, 300.00)

      def apPayment = accountPayablePaymentDataLoaderService.single(company, bank, vendor)
      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendor, apInvoice, apPayment).toList()

      when:
      def result = get("$path/${vendor.id}")

      then:
      notThrown(Exception)
      result != null
      result.vendor.id == vendor.id
      result.unpaidAmounts.balance == 200.00
   }

   void "fetch statistics with ytdPaid and ptdPaid" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), false, false).collect()

      final dateRanges = new FinancialCalendarGLAPDateRangeDTO(LocalDate.now(), LocalDate.now().plusMonths(9), LocalDate.now(), LocalDate.now().plusMonths(7))

      when:
      put("/accounting/financial-calendar/open-gl-ap", dateRanges)

      then:
      notThrown(Exception)

      when:
      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]

      def payToPmtTerm = vendorPaymentTermList[3]

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, shipVia)

      def purchaseOrderIn = purchaseOrderDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, shipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def openAccountPayableInvoiceStatus = AccountPayableInvoiceStatusTypeDataLoader.predefined().get(2)

      def apInvoice = accountPayableInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, 1000.00, employeeIn, 700.00, openAccountPayableInvoiceStatus, payToIn, store, 300.00)

      def accountPayablePaymentStatus = AccountPayablePaymentStatusTypeDataLoader.predefined().get(0)
      def paymentType = AccountPayablePaymentTypeTypeDataLoader.predefined().get(0)
      def apPayment = accountPayablePaymentDataLoaderService.single(company, bank, vendor, accountPayablePaymentStatus, paymentType, LocalDate.now(), null, 1500.00)

      def apPaymentDetails = apPaymentDetailDataLoaderService.stream(5, company, vendor, apInvoice, apPayment, 75000.00, 1000.00).toList()

      def result = get("$path/${vendor.id}")

      then:
      notThrown(Exception)
      result != null
      result.vendor.id == vendor.id
      result.ptdPaid == 375000.00
      result.ytdPaid == 375000.00
   }

   void "fetch rebates" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorList = vendorTestDataLoaderService.stream(4, company, vendorPaymentTerm, shipVia).toList()
      final vendor = vendorList[0]
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()

      final glDebitAcct = accountTestDataLoaderService.single(company)
      final glCreditAcct = accountTestDataLoaderService.single(company)
      final rebates = rebateTestDataLoaderService.stream(7, company, vendorList, glDebitAcct, glCreditAcct).toList()
      rebates.eachWithIndex{ rebate, _ ->
         rebateTestDataLoaderService.assignVendorsToRebate(rebate, vendorList)
      }

      def filterRequest = new VendorStatisticsFilterRequest(vendorId: vendor.id)

      when: 'fetch statistics'
      def result = get("$path/${vendor.id}")

      then:
      notThrown(Exception)
      result != null
      result.vendor.id == vendor.id

      when: 'fetch rebates'
      result = get("$path/rebates$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.totalElements == 7
   }

   void "fetch invoices" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()

      final StoreEntity store = storeFactoryService.store(3, company) as StoreEntity
      final vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      final shipViaList = shipViaTestDataLoaderService.stream(4, company).toList()
      final employeeList = employeeFactoryService.stream(4, company).toList()
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

      accountPayableInvoiceDataLoaderService.stream(20, company, vendor, purchaseOrderIn, null, employeeIn, null, null, vendor, store)
         .map { new AccountPayableInvoiceDTO(it)}
         .sorted { o1, o2 -> o1.id <=> o2.id }.toList()

      def filterRequest = new VendorStatisticsFilterRequest(vendorId: vendor.id)

      when: 'fetch statistics'
      def result = get("$path/${vendor.id}")

      then:
      notThrown(Exception)
      result != null
      result.vendor.id == vendor.id

      when: 'fetch invoices'
      result = get("$path/invoices$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.totalElements == 20
   }

   void "fetch purchase orders" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()

      final approvedByIn = employeeFactoryService.single(company)
      final purchaseAgentIn = employeeFactoryService.single(company)
      final StoreEntity shipToIn = storeFactoryService.store(3, company) as StoreEntity
      final paymentTermTypeIn = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendorSubmittedEmployeeIn = employeeFactoryService.single(company)
      purchaseOrderDataLoaderService.stream(
         20,
         company,
         vendor,
         approvedByIn,
         purchaseAgentIn,
         shipVia,
         shipToIn,
         paymentTermTypeIn,
         vendorSubmittedEmployeeIn
      ).map { new PurchaseOrderDTO(it)}.sorted { o1, o2 -> o1.id <=> o2.id }.toList()

      def filterRequest = new VendorStatisticsFilterRequest(vendorId: vendor.id)

      when: 'fetch statistics'
      def result = get("$path/${vendor.id}")

      then:
      notThrown(Exception)
      result != null
      result.vendor.id == vendor.id

      when: 'fetch purchase orders'
      result = get("$path/purchase-orders$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.totalElements == 20
   }
}
