package com.cynergisuite.middleware.area


import io.micronaut.context.annotation.Requires

import jakarta.inject.Singleton

class MenuTypeDataLoader {
   private static final List<MenuType> menuTypes = [

      new MenuType(
         1,
         null,
         "ACCOUNTS_PAYABLE",
         "Account Payable",
         "account.payable",
         0,
         AreaDataLoader.areaTypes().find { it.id == 1 },
         [],
         []
      ),
      new MenuType(
         2,
         null,
         "INVOICE_MAINTENANCE",
         "AP Invoice",
         "account.payable.invoice.maintenance",
         1,
         AreaDataLoader.areaTypes().find { it.id == 1 },
         [],
         []
      ),
      new MenuType(
         3,
         null,
         "RECURRING_INVOICE_MAINTENANCE",
         "Recurring Invoice",
         "recurring.invoice.maintenance",
         2,
         AreaDataLoader.areaTypes().find { it.id == 1 },
         [],
         []
      ),
      new MenuType(
         4,
         null,
         "CHECK_MAINTENANCE",
         "AP Check",
         "account.payable.check.maintenance",
         3,
         AreaDataLoader.areaTypes().find { it.id == 1 },
         [],
         []
      ),
      new MenuType(
         5,
         null,
         "AP_REPORTS",
         "AP Reports",
         "account.payable.reports",
         4,
         AreaDataLoader.areaTypes().find { it.id == 1 },
         [],
         []
      ),
      new MenuType(
         6,
         null,
         "MONTH_END",
         "Account Payable Month End",
         "account.payable.month.end",
         5,
         AreaDataLoader.areaTypes().find { it.id == 1 },
         [],
         []
      ),
      new MenuType(
         7,
         null,
         "BANK_RECONCILIATION",
         "Bank Reconciliation",
         "bank.reconciliation",
         0,
         AreaDataLoader.areaTypes().find { it.id == 2 },
         [],
         []
      ),
      new MenuType(
         8,
         null,
         "STORE_DEPOSIT_MAINTENANCE",
         "Store Deposit",
         "store.deposit.maintenance",
         1,
         AreaDataLoader.areaTypes().find { it.id == 2 },
         [],
         []
      ),
      new MenuType(
         9,
         null,
         "SEND_STORE_DEPOSIT_TO_BANK_REC",
         "Send Store Deposit to Bank Rec",
         "send.store.deposit.to.bank.rec",
         2,
         AreaDataLoader.areaTypes().find { it.id == 2 },
         [],
         []
      ),
      new MenuType(
         10,
         null,
         "CLEAR_OUTSTANDING_ITEMS",
         "Outstanding Items",
         "outstanding.items.maintenance",
         3,
         AreaDataLoader.areaTypes().find { it.id == 2 },
         [],
         []
      ),
      new MenuType(
         11,
         null,
         "RECONCILE_BANK_ACCOUNT",
         "Reconcile Bank Account",
         "reconcile.bank.account",
         4,
         AreaDataLoader.areaTypes().find { it.id == 2 },
         [],
         []
      ),
      new MenuType(
         12,
         null,
         "BANK_TRANSACTIONS_MAINTENANCE",
         "Bank Transactions",
         "bank.transactions.maintenance",
         5,
         AreaDataLoader.areaTypes().find { it.id == 2 },
         [],
         []
      ),
      new MenuType(
         13,
         null,
         "GENERAL_LEDGER",
         "General Ledger",
         "general.ledger",
         0,
         AreaDataLoader.areaTypes().find { it.id == 3 },
         [],
         []
      ),
      new MenuType(
         14,
         null,
         "ACCOUNT_INQUIRY_ANALYSIS",
         "GL Inquiry/Analysis",
         "general.ledger.inquiry.analysis",
         1,
         AreaDataLoader.areaTypes().find { it.id == 3 },
         [],
         []
      ),
      new MenuType(
         15,
         null,
         "JOURNAL_ENTRY PROCESSING",
         "Journal Entry Processing",
         "journal.entry.processing",
         2,
         AreaDataLoader.areaTypes().find { it.id == 3 },
         [],
         []
      ),
      new MenuType(
         16,
         null,
         "GL_REPORTS",
         "GL Reports",
         "general.ledger.reports",
         3,
         AreaDataLoader.areaTypes().find { it.id == 3 },
         [],
         []
      ),
      new MenuType(
         17,
         null,
         "MONTH_END_PROCESSING",
         "GL End of Month Processing",
         "general.ledger.end.of.month.processing",
         4,
         AreaDataLoader.areaTypes().find { it.id == 3 },
         [],
         []
      ),
      new MenuType(
         18,
         null,
         "UTILITIES",
         "GL Utilities",
         "general.ledger.utilities",
         5,
         AreaDataLoader.areaTypes().find { it.id == 3 },
         [],
         []
      ),
      new MenuType(
         20,
         null,
         "PO_MAINTENANCE",
         "PO",
         "purchase.order.maintenance",
         1,
         AreaDataLoader.areaTypes().find { it.id == 4 },
         [],
         []
      ),
      new MenuType(
         21,
         null,
         "PO_REPORTS",
         "PO Reports",
         "purchase.order.reports",
         2,
         AreaDataLoader.areaTypes().find { it.id == 4 },
         [],
         []
      ),
      new MenuType(
         22,
         21,
         "PO_REPORT_EXPORT",
         "PO Report (w/Export)",
         "po.report.(w/Export)",
         3,
         AreaDataLoader.areaTypes().find { it.id == 4 },
         [],
         []
      ),
      new MenuType(
         23,
         21,
         "STOCK_REORDER",
         "Stock Reorder Report",
         "stock.reorder.report",
         4,
         AreaDataLoader.areaTypes().find { it.id == 4 },
         [],
         []
      ),
      new MenuType(
         24,
         21,
         "RECEIVER_REPORT",
         "Receiver Report",
         "receiver.report",
         5,
         AreaDataLoader.areaTypes().find { it.id == 4 },
         [],
         []
      ),
      new MenuType(
         25,
         21,
         "RECEIVING_WORKSHEET",
         "Receiving Worksheet",
         "receiving.worksheet",
         6,
         AreaDataLoader.areaTypes().find { it.id == 4 },
         [],
         []
      ),
      new MenuType(
         26,
         21,
         "SPECIAL_ORDERS",
         "Special Orders",
         "special.orders",
         7,
         AreaDataLoader.areaTypes().find { it.id == 4 },
         [],
         []
      ),
      new MenuType(
         28,
         null,
         "HOME_OFFICE",
         "Home Office",
         "home.office",
         1,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         29,
         28,
         "BANK",
         "Bank",
         "bank.maintenance",
         2,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         30,
         28,
         "CHART_OF_ACCOUNT",
         "Chart of Account",
         "chart.of.account",
         3,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         31,
         28,
         "DISTRIBUTION_TEMPLATE_MAINTENANCE",
         "Distribution Template",
         "distribution.template.maintenance",
         4,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         32,
         28,
         "FINANCIAL_STATEMENT",
         "Financial Statement",
         "financial.statement",
         5,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         33,
         28,
         "GL_SOURCE_CODE",
         "GL Source Code",
         "gl.source.code",
         6,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         34,
         28,
         "SHIPVIA",
         "Ship Via",
         "ship.via.maintenance",
         7,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         35,
         28,
         "VENDOR",
         "Vendor",
         "vendor.maintenance",
         8,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         36,
         28,
         "VENDOR_GROUP",
         "Vendor Group",
         "vendor.group",
         9,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         37,
         null,
         "RESTRICTED_MASTER_CONTROL_FILES",
         "Restricted Master Control Files",
         "restricted.master.control.files",
         10,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         38,
         37,
         "COMPANY",
         "Company",
         "company.maintenance",
         11,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         39,
         28,
         "VENDOR_TERM_CODE",
         "Vendor Term Code",
         "vendor.term.code",
         12,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         40,
         37,
         "AP_CONTROL",
         "AP Control",
         "account.payable.control",
         13,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         41,
         37,
         "PO_CONTROL",
         "PO Control",
         "po.control",
         14,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
      new MenuType(
         42,
         37,
         "GL_CONTROL",
         "GL Control",
         "general.ledger.control",
         15,
         AreaDataLoader.areaTypes().find { it.id == 5 },
         [],
         []
      ),
   ]

   static List<MenuType> menuTypes() { menuTypes }
}

@Singleton
@Requires(env = ["develop", "test"])
class MenuDataLoaderService {
   def predefined() { MenuTypeDataLoader.menuTypes() }
}
