package com.cynergisuite.middleware.accounting.general.ledger.end.year.infrastructure


import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.domain.infrastructure.SimpleTransactionalSql
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountStatusFactory
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.account.AccountTypeFactory
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDetailDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDataLoaderService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDataLoader
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarCompleteDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarGLAPDateRangeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.end.month.EndMonthProceduresDTO
import com.cynergisuite.middleware.accounting.general.ledger.end.year.EndYearProceduresDTO
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDetailDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDataLoaderService
import com.cynergisuite.middleware.inload.InloadSUMGLINTVService
import com.cynergisuite.middleware.purchase.order.PurchaseOrderTestDataLoaderService
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.store.StoreDTO
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class GeneralLedgerProcedureControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/procedure"

   @Inject GeneralLedgerJournalDataLoaderService glJournalDataLoaderService
   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService
   @Inject GeneralLedgerJournalEntryDataLoaderService generalLedgerJournalEntryDataLoaderService
   @Inject GeneralLedgerReversalDataLoaderService generalLedgerReversalDataLoaderService
   @Inject InloadSUMGLINTVService inloadSUMGLINTVService
   @Inject VendorPaymentTermTestDataLoaderService vendorPaymentTermTestDataLoaderService
   @Inject ShipViaTestDataLoaderService shipViaFactoryService
   @Inject VendorTestDataLoaderService vendorTestDataLoaderService
   @Inject PurchaseOrderTestDataLoaderService purchaseOrderDataLoaderService
   @Inject AccountPayableInvoiceDataLoaderService dataLoaderService
   @Inject BankFactoryService bankFactoryService
   @Inject AccountTestDataLoaderService accountFactoryService
   @Inject AccountPayablePaymentDataLoaderService accountPayablePaymentDataLoaderService
   @Inject AccountPayablePaymentDetailDataLoaderService apPaymentDetailDataLoaderService
   @Inject AccountPayableDistributionTemplateDataLoaderService apDistTemplateDataLoaderService
   @Inject AccountPayableDistributionDetailDataLoaderService apDistDetailDataLoaderService
   @Inject SimpleTransactionalSql sql

   void "end current year with no pending journal entries" () {
      given:
      final beginDate = LocalDate.now().minusMonths(26)
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final capitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value == "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(capitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))
      final dateRanges = new FinancialCalendarGLAPDateRangeDTO(LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(2), LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1))

      def glJournalEntryDetailDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(capitalAccount), new StoreDTO(store), 1000 as BigDecimal).toList()
      def glJournalEntryDetailCreditDTOs = GeneralLedgerJournalEntryDetailDataLoader.streamDTO(2, new AccountDTO(capitalAccount), new StoreDTO(store), -1000 as BigDecimal).toList()
      glJournalEntryDetailDTOs.addAll(glJournalEntryDetailCreditDTOs)
      def glJournalEntryDTO = generalLedgerJournalEntryDataLoaderService.singleDTO(new GeneralLedgerSourceCodeDTO(glSourceCode), false, glJournalEntryDetailDTOs, false)
      generalLedgerReversalDataLoaderService.single(company, glSourceCode, beginDate)

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(HttpClientResponseException)

      when:
      put("/accounting/financial-calendar/open-gl-ap", dateRanges)

      then:
      notThrown(Exception)

      when: 'create journal entries'
      def result = post("/accounting/general-ledger/journal-entry", glJournalEntryDTO)

      then:
      notThrown(Exception)
      result != null

      when:
      post("$path/end-year", body)

      then:
      notThrown(HttpClientResponseException)
   }

   void "end current year with pending journal entries, unbalance GL, and capital account" () {
      given:
      final beginDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(26)
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final capitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value == "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      glJournalDataLoaderService.stream(12, company, capitalAccount, store, beginDate.plusYears(2).plusMonths(1), glSourceCode).toList()
      glJournalDataLoaderService.stream(1, company2, capitalAccount, store, LocalDate.now(), glSourceCode).toList()
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(capitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))
      final dateRanges = new FinancialCalendarGLAPDateRangeDTO(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(2), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(2), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1))
      generalLedgerReversalDataLoaderService.single(company, glSourceCode, beginDate)

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(HttpClientResponseException)

      when:
      put("/accounting/financial-calendar/open-gl-ap", dateRanges)

      then:
      notThrown(Exception)

      when:
      post("$path/end-year", body)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response[0].message == "Pending Journal Entries found for General Ledger fiscal year to be closed. All Pending Journal Entries for this date range (${beginDate.plusYears(2)} -> ${beginDate.plusYears(3).minusDays(1)}) must be posted before the General Ledger fiscal year can be closed."
      response[0].code == 'cynergi.validation.pending.jes.found.for.current.year'
      response[1].message == 'GL is NOT in Balance'
      response[1].code == 'cynergi.validation.gl.not.in.balance'
   }

   void "end current year with pending journal entries, reversal entries, unbalance GL, and non-capital account" () {
      given:
      final beginDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(26)
      final financialCalendarDTO = new FinancialCalendarCompleteDTO([periodFrom: beginDate])
      final company = companyFactoryService.forDatasetCode('coravt')
      final company2 = companyFactoryService.forDatasetCode('corrto')
      final nonCapitalAccount = accountDataLoaderService.single(company, AccountStatusFactory.predefined().find {it.value == "A" }, AccountTypeFactory.predefined().find {it.value != "C" })
      final store = storeFactoryService.store(3, company)
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(company, "BAL")
      glJournalDataLoaderService.stream(12, company, nonCapitalAccount, store, beginDate.plusYears(2).plusMonths(1), glSourceCode).toList()
      glJournalDataLoaderService.stream(1, company2, nonCapitalAccount, store, LocalDate.now(), glSourceCode).toList()
      def body = new EndYearProceduresDTO(new SimpleIdentifiableDTO(nonCapitalAccount), new SimpleLegacyIdentifiableDTO(store.myId()))
      final dateRanges = new FinancialCalendarGLAPDateRangeDTO(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(2), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(2), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1), LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1))
      generalLedgerReversalDataLoaderService.single(company, glSourceCode, LocalDate.now())

      generalLedgerSourceCodeDataLoaderService.stream(1, company, "MEC").toList()

      def format = CSVFormat.DEFAULT
         .builder()
         .setHeader(*['Data_Set_ID', 'Store_Number', 'Date', 'Verify_Successful', 'Error_Amount',
                      'Dep_Cash_Amt', 'Dep_Cash_GL_Acct_Nbr', 'Dep_For_Oth_Str_Amt', 'Dep_For_Oth_Str_GL_Acct_Nbr',
                      'Dep_From_Oth_Str_Amt', 'Dep_From_Oth_Str_GL_Acct_Nbr', 'Dep_CC_In_Str_Amt', 'Dep_CC_In_Str_GL_Acct_Nbr',
                      'Dep_ACH_OLP_Amt', 'Dep_ACH_OLP_GL_Acct_Nbr', 'Dep_CC_OLP_Amt', 'Dep_CC_OLP_GL_Acct_Nbr',
                      'Dep_Debit_Card_Amt', 'Dep_Debit_Card_GL_Acct_Nbr', 'NSF_Return_Check_Amt', 'NSF_Return_Check_GL_Acct_Nbr',
                      'AR_Bad_Check_Amt', 'AR_Bad_Check_GL_Acct_Nbr', 'ICC_Chargeback_Amt', 'ICC_Chargeback_GL_Acct_Nbr',
                      'ACH_Chargeback_Amt', 'ACH_Chargeback_GL_Acct_Nbr'])
         .build()
      def csvData = """
         coravt,1,${LocalDate.now().toString()},true,000000000.00,000001050.28,${nonCapitalAccount.number},001016,${nonCapitalAccount.number},151.35,${nonCapitalAccount.number},3058.63,${nonCapitalAccount.number},10.00,${nonCapitalAccount.number},548.74,${nonCapitalAccount.number},-151.35,${nonCapitalAccount.number},4657.65,${nonCapitalAccount.number},46.00,${nonCapitalAccount.number},3.00,${nonCapitalAccount.number},4.00,${nonCapitalAccount.number}
         """
      def parser = CSVParser.parse(csvData, format)
      def record = parser.getRecords().get(0)

      when:
      inloadSUMGLINTVService.inloadCsv(record, UUID.randomUUID())

      then:
      notThrown(Exception)

      when:
      post("/accounting/financial-calendar/complete", financialCalendarDTO)

      then:
      notThrown(HttpClientResponseException)

      when:
      put("/accounting/financial-calendar/open-gl-ap", dateRanges)

      then:
      notThrown(Exception)

      when:
      post("$path/end-year", body)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 5
      response[0].message == "Pending Journal Entries found for General Ledger fiscal year to be closed. All Pending Journal Entries for this date range (${beginDate.plusYears(2)} -> ${beginDate.plusYears(3).minusDays(1)}) must be posted before the General Ledger fiscal year can be closed."
      response[0].code == 'cynergi.validation.pending.jes.found.for.current.year'
      response[1].message == "Reversal Journal Entries found for General Ledger fiscal year to be closed. All Reversal Journal Entries for this date range must be posted before the General Ledger fiscal year can be closed."
      response[1].code == 'cynergi.validation.unposted.reversals.found.for.current.year'
      response[2].message == "SUMGLINT Staging Entries found for General Ledger fiscal year to be closed. All SUMGLINT Staging  entries for this date range must be moved to Pending Journal Entries and must be posted before the General Ledger fiscal year can be closed."
      response[2].code == 'cynergi.validation.unposted.verify.stagings.found.for.current.year'
      response[3].message == 'Must be capital account'
      response[3].code == 'cynergi.validation.must.be'
      response[3].path == 'account'
      response[4].message == 'GL is NOT in Balance'
      response[4].code == 'cynergi.validation.gl.not.in.balance'
   }

   void "test AP Month End procedure" () {
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

      def apInvoiceEntities = dataLoaderService.stream(5, company, vendorIn, purchaseOrderIn, null, employeeIn, null, statusO, payToIn, store)
         .toList()

      def apInvoices = apInvoiceEntities.stream().map { new AccountPayableInvoiceDTO(it) }.toList()

      def account = accountFactoryService.single(company)
      def bank = bankFactoryService.single(nineNineEightEmployee.company, store, account)
      def pmtStatuses = AccountPayablePaymentStatusTypeDataLoader.predefined()
      def pmtTypes = AccountPayablePaymentTypeTypeDataLoader.predefined()

      def apPayments = accountPayablePaymentDataLoaderService.stream(2, company, bank, vendorIn, pmtStatuses.find { it.value == 'P' }, pmtTypes.find { it.value == 'A' }).toList().sort { o1, o2 -> o1.paymentNumber <=> o2.paymentNumber }
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

      def body = new EndMonthProceduresDTO(null, null,null, null,null, null, LocalDate.of(2020, 1, 1), LocalDate.now().plusDays(5), false, ['O', 'P'], null, 'account', null)

      when:
      def result = post("$path/end-month", body)

      then:
      notThrown(Exception)
      with(result.groupedByAccount[1]) {
         accountNumber == account.number
         accountName == account.name
         glAmountTotal == invDistAmountForFirstInvoice + invDistAmountForSecondInvoice
         groupedByDistributionCenters.eachWithIndex { profitCenter, index ->
            with(profitCenter) {
               distCenter == store.number
               glAmountTotal == invDistAmountForFirstInvoice + invDistAmountForSecondInvoice
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

}
