package com.cynergisuite.middleware.accounting.general.ledger.reconciliation

import com.cynergisuite.domain.GeneralLedgerReconciliationReportFilterRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDataLoaderService
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDataLoaderService
import com.cynergisuite.middleware.inventory.InventoryEndOfMonthDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.LocalDate

@MicronautTest(transactional = false)

class GeneralLedgerReconciliationControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/reconciliation"

   @Inject GeneralLedgerSummaryDataLoaderService dataLoaderService
   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject FinancialCalendarDataLoaderService financialCalendarDataLoaderService
   @Inject InventoryEndOfMonthDataLoaderService inventoryEndOfMonthDataLoaderService
   @Inject BankFactoryService bankFactoryService


   void "filter for gl recon report" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final accountList = accountDataLoaderService.stream(12, company).toList()
      final store = storeFactoryService.store(3, company)
      final bank = bankFactoryService.single(company, store, accountList[0] as AccountEntity)
      financialCalendarDataLoaderService.streamFiscalYear(company, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" }, LocalDate.of(2023, 01, 01), false, false).collect()
      final date = LocalDate.now()
      final inv1 = inventoryEndOfMonthDataLoaderService.stream(1, company, accountList[0] as AccountEntity, accountList[1] as AccountEntity, store, date).collect()
      final inv2 = inventoryEndOfMonthDataLoaderService.stream(1, company, accountList[0] as AccountEntity, accountList[1] as AccountEntity, store, date).collect()

      final glSummary1 = dataLoaderService.single(company, accountList[0] as AccountEntity, store, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" })
      final glSummary2 = dataLoaderService.single(company, accountList[1] as AccountEntity, store, OverallPeriodTypeDataLoader.predefined().find { it.value == "C" })

      final inv3 = inventoryEndOfMonthDataLoaderService.stream(1, company, accountList[0] as AccountEntity, accountList[1] as AccountEntity, store, date.minusMonths(1)).collect()
      final inv4 = inventoryEndOfMonthDataLoaderService.stream(1, company, accountList[0] as AccountEntity, accountList[1] as AccountEntity, store, date.minusMonths(2)).collect()
      def filterRequest = new GeneralLedgerReconciliationReportFilterRequest()
      filterRequest['date'] = date

      when:
      def response = get("$path$filterRequest")

      then:
      notThrown(Exception)
      response != null
      with(response) {
         with(inventory[0]) {
            accountType == "Asset"
         }
         with(inventory[1]) {
            accountType == 'Contra'
         }
      }
   }
}
