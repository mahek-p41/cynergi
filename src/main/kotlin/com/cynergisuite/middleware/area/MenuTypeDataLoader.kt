package com.cynergisuite.middleware.area

import com.cynergisuite.extensions.forId
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object MenuTypeDataLoader {
   // TODO write groovy support script to build these from the DB
   private val menuTypes = listOf(
      MenuType(
         id = 1,
         value = "ACCOUNTS_PAYABLE",
         description = "Account Payable",
         localizationCode = "account.payable",
         orderNumber = 0,
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 2,
         value = "INVOICE_MAINTENANCE",
         description = "AP Invoice",
         localizationCode = "account.payable.invoice.maintenance",
         orderNumber = 1,
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 3,
         value = "RECURRING_INVOICE_MAINTENANCE",
         description = "Recurring Invoice",
         localizationCode = "recurring.invoice.maintenance",
         orderNumber = 2,
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 4,
         value = "CHECK_MAINTENANCE",
         description = "AP Check",
         localizationCode = "account.payable.check.maintenance",
         orderNumber = 3,
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 5,
         value = "AP_REPORTS",
         description = "AP Reports",
         localizationCode = "account.payable.reports",
         orderNumber = 4,
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 6,
         value = "MONTH_END",
         description = "Account Payable Month End",
         localizationCode = "account.payable.month.end",
         orderNumber = 5,
         areaType = AreaDataLoader.areaTypes().forId(1)
      ),
      MenuType(
         id = 7,
         value = "BANK_RECONCILIATION",
         description = "Bank Reconciliation",
         localizationCode = "bank.reconciliation",
         orderNumber = 0,
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 8,
         value = "STORE_DEPOSIT_MAINTENANCE",
         description = "Store Deposit",
         localizationCode = "store.deposit.maintenance",
         orderNumber = 1,
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 9,
         value = "SEND_STORE_DEPOSIT_TO_BANK_REC",
         description = "Send Store Deposit to Bank Rec",
         localizationCode = "send.store.deposit.to.bank.rec",
         orderNumber = 2,
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 10,
         value = "CLEAR_OUTSTANDING_ITEMS",
         description = "Outstanding Items",
         localizationCode = "outstanding.items.maintenance",
         orderNumber = 3,
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 11,
         value = "RECONCILE_BANK_ACCOUNT",
         description = "Reconcile Bank Account",
         localizationCode = "reconcile.bank.account",
         orderNumber = 4,
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 12,
         value = "BANK_TRANSACTIONS_MAINTENANCE",
         description = "Bank Transactions",
         localizationCode = "bank.transactions.maintenance",
         orderNumber = 5,
         areaType = AreaDataLoader.areaTypes().forId(2)
      ),
      MenuType(
         id = 13,
         value = "GENERAL_LEDGER",
         description = "General Ledger",
         localizationCode = "general.ledger",
         orderNumber = 0,
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 14,
         value = "ACCOUNT_INQUIRY_ANALYSIS",
         description = "GL Inquiry/Analysis",
         localizationCode = "general.ledger.inquiry.analysis",
         orderNumber = 1,
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 15,
         value = "JOURNAL_ENTRY PROCESSING",
         description = "Journal Entry Processing",
         localizationCode = "journal.entry.processing",
         orderNumber = 2,
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 16,
         value = "GL_REPORTS",
         description = "GL Reports",
         localizationCode = "general.ledger.reports",
         orderNumber = 3,
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 17,
         value = "MONTH_END_PROCESSING",
         description = "GL End of Month Processing",
         localizationCode = "general.ledger.end.of.month.processing",
         orderNumber = 4,
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 18,
         value = "UTILITIES",
         description = "GL Utilities",
         localizationCode = "general.ledger.utilities",
         orderNumber = 5,
         areaType = AreaDataLoader.areaTypes().forId(3)
      ),
      MenuType(
         id = 19,
         value = "PURCHASE_ORDER",
         description = "Purchase Order",
         localizationCode = "purchase.order",
         orderNumber = 0,
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 20,
         value = "PO_MAINTENANCE",
         description = "PO",
         localizationCode = "purchase.order.maintenance",
         orderNumber = 1,
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 21,
         value = "PO_REPORTS",
         description = "PO Reports",
         localizationCode = "purchase.order.reports",
         orderNumber = 2,
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 22,
         parent_id = 21,
         value = "PO_REPORT_EXPORT",
         description = "PO Report (w/Export)",
         localizationCode = "po.report.(w/Export)",
         orderNumber = 3,
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 23,
         parent_id = 21,
         value = "STOCK_REORDER",
         description = "Stock Reorder Report",
         localizationCode = "stock.reorder.report",
         orderNumber = 4,
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 24,
         parent_id = 21,
         value = "RECEIVER_REPORT",
         description = "Receiver Report",
         localizationCode = "receiver.report",
         orderNumber = 5,
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 25,
         parent_id = 21,
         value = "RECEIVING_WORKSHEET",
         description = "Receiving Worksheet",
         localizationCode = "receiving.worksheet",
         orderNumber = 6,
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 26,
         parent_id = 21,
         value = "SPECIAL_ORDERS",
         description = "Special Orders",
         localizationCode = "special.orders",
         orderNumber = 7,
         areaType = AreaDataLoader.areaTypes().forId(4)
      ),
      MenuType(
         id = 27,
         value = "MASTER_CONTROL_FILE_MAINTENANCE",
         description = "MCF",
         localizationCode = "master.control.file.maintenance",
         orderNumber = 0,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 28,
         value = "HOME_OFFICE",
         description = "HOME OFFICE",
         localizationCode = "home.office",
         orderNumber = 0,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 29,
         value = "BANK",
         description = "Bank",
         localizationCode = "bank.maintenance",
         orderNumber = 1,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 30,
         value = "CHART_OF_ACCOUNT",
         description = "Chart of Account",
         localizationCode = "chart.of.account",
         orderNumber = 2,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 31,
         value = "DISTRIBUTION_TEMPLATE_MAINTENANCE",
         description = "Distribution Template",
         localizationCode = "distribution.template.maintenance",
         orderNumber = 3,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 32,
         value = "FINANCIAL_STATEMENT",
         description = "Financial Statement",
         localizationCode = "financial.statement",
         orderNumber = 4,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 33,
         value = "GL_SOURCE_CODE",
         description = "GL Source Code",
         localizationCode = "gl.source.code",
         orderNumber = 5,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 34,
         value = "SHIPVIA",
         description = "Ship Via",
         localizationCode = "ship.via.maintenance",
         orderNumber = 6,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 35,
         value = "VENDOR",
         description = "Vendor",
         localizationCode = "vendor.maintenance",
         orderNumber = 7,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 36,
         value = "RESTRICTED_MASTER_CONTROL_FILES",
         description = "RESTRICTED MASTER CONTROL FILES",
         localizationCode = "restricted.master.control.files",
         orderNumber = 8,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 37,
         value = "COMPANY",
         description = "Company",
         localizationCode = "company.maintenance",
         orderNumber = 9,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 38,
         value = "DIVISION",
         description = "Division",
         localizationCode = "division",
         orderNumber = 10,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 39,
         value = "VENDOR_TERM_CODE",
         description = "Vendor Term Code",
         localizationCode = "vendor.term.code",
         orderNumber = 11,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 40,
         value = "REGION",
         description = "Region",
         localizationCode = "region",
         orderNumber = 12,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 41,
         value = "AP_CONTROL",
         description = "AP Control",
         localizationCode = "account.payable.control",
         orderNumber = 13,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 42,
         value = "PO_CONTROL",
         description = "PO Control",
         localizationCode = "po.control",
         orderNumber = 14,
         areaType = AreaDataLoader.areaTypes().forId(5)
      ),
      MenuType(
         id = 43,
         value = "GL_CONTROL",
         description = "GL Control",
         localizationCode = "general.ledger.control",
         orderNumber = 15,
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
