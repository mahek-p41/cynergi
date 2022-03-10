package com.cynergisuite.middleware.accounting.account.payable.aging.infrastructure

import com.cynergisuite.domain.AgingReportFilterRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.LocalDate

@MicronautTest(transactional = false)
class AccountPayableAgingReportControllerSpecification extends ControllerSpecificationBase {
   private static String path = "/accounting/account-payable/aging"

   @Inject AccountTestDataLoaderService accountTestDataLoaderService
   @Inject AccountPayableInvoiceDataLoaderService apInvoiceDataLoaderService
   @Inject AccountPayablePaymentDataLoaderService apPaymentDataLoaderService
   @Inject AccountPayablePaymentDetailDataLoaderService apPaymentDetailDataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject PurchaseOrderTestDataLoaderService poTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaTestDataLoaderService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService

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
      def vendor3PmtTerm = vendorPaymentTermList[0]
      def vendor3ShipVia = shipViaList[1]
      def vendor3 = vendorTestDataLoaderService.single(company, vendor3PmtTerm, vendor3ShipVia)

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
      def apInvoicesForVend1 = apInvoiceDataLoaderService.stream(4, company, vendor1, purchaseOrderIn1, null, employeeIn, null, null, payToIn, store).toList()
      def apInvoicesForVend2 = apInvoiceDataLoaderService.stream(4, company, vendor2, purchaseOrderIn2, null, employeeIn, null, null, payToIn, store).toList()
      def apInvoicesForVend3 = apInvoiceDataLoaderService.stream(4, company, vendor3, purchaseOrderIn3, null, employeeIn, null, null, payToIn, store).toList()

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

      def agingDate = LocalDate.now()

      def filterRequest = new AgingReportFilterRequest([sortBy: "id", sortDirection: "ASC"])
      switch (criteria) {
         case 'Vendors by vendorStart':
            filterRequest['vendorStart'] = vendor1.number
            filterRequest['agingDate'] = agingDate
            break
         case 'Vendors by vendorEnd':
            filterRequest['vendorEnd'] = vendor2.number
            filterRequest['agingDate'] = agingDate
            break
         case 'Vendors by vendorStart and vendorEnd':
            filterRequest['vendorStart'] = vendor2.number
            filterRequest['vendorEnd'] = vendor3.number
            filterRequest['agingDate'] = agingDate
            break
         case 'All vendors by vendorStart and vendorEnd':
            filterRequest['vendorStart'] = vendor1.number
            filterRequest['vendorEnd'] = vendor3.number
            filterRequest['agingDate'] = agingDate
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
      'All vendors by vendorStart and vendorEnd'   || 3
   }

   void "fetch all with multiple payment details and back date inquiry"() {
      given:
      def company = companyFactoryService.forDatasetCode('tstds1')
      def store = storeFactoryService.store(3, company)
      def vendorPaymentTermList = vendorPaymentTermTestDataLoaderService.stream(4, company).toList()
      def shipViaList = shipViaTestDataLoaderService.stream(4, company).toList()

      def vendorPmtTerm = vendorPaymentTermList[0]
      def vendorShipVia = shipViaList[0]
      def vendor = vendorTestDataLoaderService.single(company, vendorPmtTerm, vendorShipVia)

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn = poTestDataLoaderService.single(company, vendor, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def statusTypeIn = new AccountPayableInvoiceStatusType(3, "P", "Paid", "paid")
      def apInvoice = apInvoiceDataLoaderService.single(company, vendor, purchaseOrderIn, 15000, employeeIn, 15000, statusTypeIn, payToIn, store)

      def account = accountTestDataLoaderService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      // 2 payments of $4000 should be taken into account at time of report
      def apPayments = apPaymentDataLoaderService.stream(2, company, bank, vendor, null, null, LocalDate.now(), null, 4000).toList()
      // payment of $7000 should not be taken into account because of the future date
      apPayments.add(apPaymentDataLoaderService.single(company, bank, vendor, null, null, LocalDate.now().plusDays(20), null, 7000))
      // payment of $1100 should not be taken into account because it is voided
      apPayments.add(apPaymentDataLoaderService.single(company, bank, vendor, null, null, LocalDate.now(), LocalDate.now(), 1100))

      apPaymentDetailDataLoaderService.single(company, vendor, apInvoice, apPayments[0], 4000)
      apPaymentDetailDataLoaderService.single(company, vendor, apInvoice, apPayments[1], 4000)
      apPaymentDetailDataLoaderService.single(company, vendor, apInvoice, apPayments[2], 7000)
      apPaymentDetailDataLoaderService.single(company, vendor, apInvoice, apPayments[3], 1100)

      // report is run for 10 days from now
      def agingDate = LocalDate.now().plusDays(10)

      def filterRequest = new AgingReportFilterRequest([sortBy: "id", sortDirection: "ASC"])
      filterRequest['agingDate'] = agingDate

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.vendors.size() == 1
      result.agedTotals.balanceTotal == 7000.00 // remaining balance
   }

   void "fetch all with multiple vendors, invoices, and payment details and back date inquiry"() {
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

      def employeeList = employeeFactoryService.stream(4, company).toList()
      def poApprovedBy = employeeList[0]
      def poPurchaseAgent = employeeList[1]
      def poShipVia = shipViaList[2]
      def poPmtTerm = vendorPaymentTermList[2]
      def poVendorSubEmp = employeeList[2]
      def purchaseOrderIn1 = poTestDataLoaderService.single(company, vendor1, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def purchaseOrderIn2 = poTestDataLoaderService.single(company, vendor2, poApprovedBy, poPurchaseAgent, poShipVia, store, poPmtTerm, poVendorSubEmp)
      def employeeIn = employeeList[3]

      def payToPmtTerm = vendorPaymentTermList[3]
      def payToShipVia = shipViaList[3]
      def payToIn = vendorTestDataLoaderService.single(company, payToPmtTerm, payToShipVia)
      def statusTypeO = new AccountPayableInvoiceStatusType(2, "O", "Open", "open")
      def statusTypeP = new AccountPayableInvoiceStatusType(3, "P", "Paid", "paid")
      def apInvoicesForVend1 = apInvoiceDataLoaderService.stream(4, company, vendor1, purchaseOrderIn1, 15000, employeeIn, 15000, statusTypeP, payToIn, store).toList()
      def apInvoicesForVend2 = apInvoiceDataLoaderService.stream(4, company, vendor2, purchaseOrderIn2, 2000, employeeIn, 0, statusTypeO, payToIn, store).toList()

      def account = accountTestDataLoaderService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)

      // payments for vendor1's 4 invoices
      def apPaymentsForVend1 = []
      // 2 payments of $4000 should be taken into account at time of report
      apPaymentsForVend1.add(apPaymentDataLoaderService.single(company, bank, vendor1, null, null, LocalDate.now(), null, 4000))
      apPaymentsForVend1.add(apPaymentDataLoaderService.single(company, bank, vendor1, null, null, LocalDate.now(), null, 4000))
      // payment of $7000 should not be taken into account because of the future date
      apPaymentsForVend1.add(apPaymentDataLoaderService.single(company, bank, vendor1, null, null, LocalDate.now().plusDays(20), null, 7000))
      // payment of $1100 should not be taken into account because it is voided
      apPaymentsForVend1.add(apPaymentDataLoaderService.single(company, bank, vendor1, null, null, LocalDate.now(), LocalDate.now(), 1100))
      def paymentAmounts = [4000, 4000, 7000, 1100]
      apInvoicesForVend1.each { invoice ->
         apPaymentsForVend1.eachWithIndex { payment, index ->
            apPaymentDetailDataLoaderService.single(company, vendor1, invoice, payment, paymentAmounts[index].toBigDecimal())
         }
      }

      // report is run for 10 days from now
      def agingDate = LocalDate.now().plusDays(10)

      def filterRequest = new AgingReportFilterRequest([sortBy: "id", sortDirection: "ASC"])
      filterRequest['agingDate'] = agingDate

      when:
      def result = get("$path$filterRequest")

      then:
      notThrown(Exception)
      result != null
      result.vendors.size() == 2
      result.vendors.vendorTotals.balanceTotal[0] == 28000
      result.vendors.vendorTotals.balanceTotal[1] == 8000
      result.agedTotals.balanceTotal == 36000
   }
}
