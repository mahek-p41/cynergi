package com.cynergisuite.middleware.area

import com.cynergisuite.extensions.forId
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object MenuTypeDataLoader {
   private val menuTypes = listOf(
      MenuType(
         id = 1,
         value = "ACCOUNTS PAYABLE",
         description = "Account Payable",
         localizationCode = "account.payable.menu",
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 2,
         value = "BANK RECONCILIATION",
         description = "Bank Reconciliation",
         localizationCode = "bank.reconciliation.menu",
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 3,
         value = "GENERAL LEDGER",
         description = "General Ledger",
         localizationCode = "general.ledger.menu",
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 4,
         value = "PURCHASE ORDER",
         description = "Purchase Order",
         localizationCode = "purchase.order.menu",
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 5,
         value = "INVOICE MAINTENANCE",
         description = "AP Invoice Maintenance",
         localizationCode = "account.payable.invoice.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 6,
         value = "CHECK MAINTENANCE",
         description = "AP Check Maintenance",
         localizationCode = "account.payable.check.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 7,
         value = "REPORTS MENU",
         description = "AP Reports",
         localizationCode = "account.payable.reports.menu",
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 8,
         value = "PO MAINTENANCE",
         description = "PO Maintenance",
         localizationCode = "purchase.order.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 9,
         value = "PO REPORTS",
         description = "PO Reports",
         localizationCode = "purchase.order.reports",
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 10,
         value = "MASTER CONTROL FILE MAINTENANCE",
         description = "Master Control File Maintenance",
         localizationCode = "master.control.file.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 11,
         value = "STORE DEPOSIT MAINTENANCEv",
         description = "Store Deposit Maintenance",
         localizationCode = "store.deposit.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 12,
         value = "SEND STORE DEPOSIT TO BANK REC",
         description = "Complete Store Deposit Process With Bank",
         localizationCode = "complete.store.deposit.process.with.bank",
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 13,
         value = "CLEAR OUTSTANDING ITEMS",
         description = "Outstanding Items Maintenance",
         localizationCode = "outstanding.items.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 14,
         value = "BANK TRANSACTIONS MAINTENANCE",
         description = "Bank Transactions Maintenance",
         localizationCode = "bank.transactions.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 15,
         value = "RECONCILE BANK ACCOUNT",
         description = "Reconcile Bank Account",
         localizationCode = "reconcile.bank.account",
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 16,
         value = "BANK MAINTENANCE",
         description = "Bank Maintenance",
         localizationCode = "bank.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 17,
         value = "ACCOUNT INQUIRY/ANALYSIS",
         description = "General Ledger Inquiry/Analysis",
         localizationCode = "general.ledger.inquiry.analysis",
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 18,
         value = "JOURNAL ENTRY PROCESSING",
         description = "Journal Entry Processing",
         localizationCode = "journal.entry.processing",
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 19,
         value = "GL REPORTS",
         description = "General Ledger Reports",
         localizationCode = "general.ledger.reports",
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 20,
         value = "MONTH END PROCESSING",
         description = "General Ledger End of Month Processing",
         localizationCode = "general.ledger.end.of.month.processing",
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 21,
         value = "UTILITIES",
         description = "Allows Changes To Existing Purchase Orders",
         localizationCode = "allows.changes.to.existing.purchase.orders",
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 22,
         value = "COMPANY",
         description = "Company Maintenance Menu",
         localizationCode = "company.maintenance.menu",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 23,
         value = "REGION",
         description = "Region Menu",
         localizationCode = "region.menu",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 24,
         value = "DIVISION",
         description = "Division Menu",
         localizationCode = "division.menu",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 25,
         value = "PO CONTROL",
         description = "PO Control",
         localizationCode = "po.control.menu",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 26,
         value = "VENDOR",
         description = "Vendor Maintenance",
         localizationCode = "vendor.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 27,
         value = "SHIP VIA",
         description = "Ship Via Maintenance",
         localizationCode = "ship.via.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 28,
         value = "VENDOR TERM CODE",
         description = "Vendor Term Code",
         localizationCode = "vendor.term.code",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 29,
         value = "AP CONTROL",
         description = "Account Payable Control",
         localizationCode = "account.payable.control",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 30,
         value = "CHART OF ACCOUNT",
         description = "Chart of Account'",
         localizationCode = "chart.of.account",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 31,
         value = "BANK",
         description = "Bank Maintenance",
         localizationCode = "bank.maintenance",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 32,
         value = "GL CONTROL",
         description = "General Ledger Control",
         localizationCode = "general.ledger.control",
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 33,
         value = "FINANCIAL STATEMENT",
         description = "Financial Statement",
         localizationCode = "financial.statement",
         areaType = AreaDataLoader.areaTypes().forId(5)
      )
   )

   @JvmStatic
   fun menuTypes(): List<MenuType> = menuTypes

}

@Singleton
@Requires(env = ["develop", "test"])
class MenuDataLoaderService {
   fun predefined() = MenuTypeDataLoader.menuTypes()
}
