package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.VendorStatisticsFilterRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import com.cynergisuite.middleware.vendor.rebate.RebateTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.LocalDate

@MicronautTest(transactional = false)
class VendorStatisticsControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor-statistics"

   @Inject AccountPayableInvoiceDataLoaderService accountPayableInvoiceDataLoaderService
   @Inject AccountTestDataLoaderService accountTestDataLoaderService
   @Inject FinancialCalendarDataLoaderService financialCalendarDataLoaderService
   @Inject PurchaseOrderTestDataLoaderService purchaseOrderDataLoaderService
   @Inject RebateTestDataLoaderService rebateTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService

   void "fetch statistics" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final shipVia = shipViaTestDataLoaderService.single(company)
      final vendorPaymentTerm = vendorPaymentTermTestDataLoaderService.singleWithSingle90DaysPayment(company)
      final vendor = vendorTestDataLoaderService.single(company, vendorPaymentTerm, shipVia)

      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.now(), true, true).collect()

      when:
      def result = get("$path/${vendor.id}")

      then:
      notThrown(Exception)
      result != null
      result.vendor.id == vendor.id
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

      final store = storeFactoryService.store(3, company)
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
      final shipToIn = storeFactoryService.store(3, company)
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
