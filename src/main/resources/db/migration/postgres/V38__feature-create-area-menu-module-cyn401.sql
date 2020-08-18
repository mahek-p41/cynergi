-- being domain tables
CREATE TABLE area_type_domain
(
    id                 INTEGER                                                        NOT NULL PRIMARY KEY,
    value              VARCHAR(50) CHECK ( char_length(trim(value)) > 1)              NOT NULL,
    description        VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code  VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO area_type_domain (id, value, description, localization_code)
VALUES (1, 'AP', 'ACCOUNT PAYABLE', 'account.payable'),
       (2, 'BR', 'BANK RECONCILIATION', 'bank.reconciliation'),
       (3, 'GL', 'GENERAL LEDGER', 'general.ledger'),
       (4, 'PO', 'PURCHASE ORDER', 'purchase.order'),
       (5, 'MCF', 'MASTER CONTROL FILES', 'master.control.files');

CREATE TABLE menu_type_domain
(
    id                INTEGER                                                       NOT NULL PRIMARY KEY,
    value             VARCHAR(50) CHECK (char_length(trim(value)) > 1)              NOT NULL,
    description       VARCHAR(100) CHECK (char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK (char_length(trim(localization_code)) > 1) NOT NULL,
    area_type_id      BIGINT REFERENCES area_type_domain (id)                       NOT NULL,
    order_number     INTEGER DEFAULT 0,
    UNIQUE (area_type_id, value)
);

INSERT INTO menu_type_domain (id, value, description, localization_code, area_type_id, order_number)
VALUES (1, 'ACCOUNTS_PAYABLE', 'Account Payable', 'account.payable', (SELECT id FROM area_type_domain WHERE value = 'AP'), 0),
       (2, 'INVOICE_MAINTENANCE', 'AP Invoice Maintenance', 'account.payable.invoice.maintenance', (SELECT id FROM area_type_domain WHERE value = 'AP'), 1),
       (3, 'RECURRING_INVOICE_MAINTENANCE', 'Recurring Invoice Maintenance', 'recurring.invoice.maintenance', (SELECT id FROM area_type_domain WHERE value = 'AP'), 2),
       (4, 'CHECK_MAINTENANCE', 'AP Check Maintenance', 'account.payable.check.maintenance', (SELECT id FROM area_type_domain WHERE value = 'AP'), 3),
       (5, 'AP_REPORTS', 'AP Reports', 'account.payable.reports', (SELECT id FROM area_type_domain WHERE value = 'AP'), 4),
       (6, 'MONTH_END', 'Account Payable Month End', 'account.payable.month.end', (SELECT id FROM area_type_domain WHERE value = 'AP'), 5),

       (7, 'BANK_RECONCILIATION', 'Bank Reconciliation', 'bank.reconciliation', (SELECT id FROM area_type_domain WHERE value = 'BR'), 0),
       (8, 'STORE_DEPOSIT_MAINTENANCE', 'Store Deposit Maintenance', 'store.deposit.maintenance', (SELECT id FROM area_type_domain WHERE value = 'BR'), 1),
       (9, 'SEND_STORE_DEPOSIT_TO_BANK_REC', 'Send Store Deposit to Bank Rec', 'send.store.deposit.to.bank.rec', (SELECT id FROM area_type_domain WHERE value = 'BR'), (SELECT id FROM area_type_domain WHERE value = 'BR')),
       (10, 'CLEAR_OUTSTANDING_ITEMS', 'Outstanding Items Maintenance', 'outstanding.items.maintenance', (SELECT id FROM area_type_domain WHERE value = 'BR'), 3),
       (11, 'RECONCILE_BANK_ACCOUNT', 'Reconcile Bank Account', 'reconcile.bank.account', (SELECT id FROM area_type_domain WHERE value = 'BR'), 4),
       (12, 'BANK_TRANSACTIONS_MAINTENANCE', 'Bank Transactions Maintenance', 'bank.transactions.maintenance', (SELECT id FROM area_type_domain WHERE value = 'BR'), 5),

       (13, 'GENERAL_LEDGER', 'General Ledger', 'general.ledger', (SELECT id FROM area_type_domain WHERE value = 'GL'), 0),
       (14, 'ACCOUNT_INQUIRY_ANALYSIS', 'GL Inquiry/Analysis', 'general.ledger.inquiry.analysis', (SELECT id FROM area_type_domain WHERE value = 'GL'), 1),
       (15, 'JOURNAL_ENTRY PROCESSING', 'Journal Entry Processing', 'journal.entry.processing', (SELECT id FROM area_type_domain WHERE value = 'GL'), 2),
       (16, 'GL_REPORTS', 'GL Reports', 'general.ledger.reports', (SELECT id FROM area_type_domain WHERE value = 'GL'), (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (17, 'MONTH_END_PROCESSING', 'GL End of Month Processing', 'general.ledger.end.of.month.processing', (SELECT id FROM area_type_domain WHERE value = 'GL'), 4),
       (18, 'UTILITIES', 'GL Utilities', 'general.ledger.utilities', (SELECT id FROM area_type_domain WHERE value = 'GL'), 5),

       (19, 'PURCHASE_ORDER', 'Purchase Order', 'purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO'), 0),
       (20, 'PO_MAINTENANCE', 'PO Maintenance', 'purchase.order.maintenance', (SELECT id FROM area_type_domain WHERE value = 'PO'), 1),
       (21, 'PO_REPORTS', 'PO Reports', 'purchase.order.reports', (SELECT id FROM area_type_domain WHERE value = 'PO'), 2),
       (22, 'PO_REPORT_EXPORT', 'PO Report (w/Export)', 'po.report.(w/Export)', (SELECT id FROM area_type_domain WHERE value = 'PO'), 3),
       (23, 'STOCK_REORDER', 'STOCK REORDER REPORT', 'stock.reorder.report', (SELECT id FROM area_type_domain WHERE value = 'PO'), 4),
       (24, 'RECEIVER_REPORT', 'RECEIVER REPORT', 'receiver.report', (SELECT id FROM area_type_domain WHERE value = 'PO'), 5),
       (25, 'RECEIVING_WORKSHEET', 'RECEIVING WORKSHEET', 'receiving.worksheet', (SELECT id FROM area_type_domain WHERE value = 'PO'), 6),
       (26, 'SPECIAL_ORDERS', 'SPECIAL ORDERS', 'special.orders', (SELECT id FROM area_type_domain WHERE value = 'PO'), 7),

       (27, 'MASTER_CONTROL_FILE_MAINTENANCE', 'MCF Maintenance', 'master.control.file.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 0),
       (28, 'HOME_OFFICE', 'HOME OFFICE', 'home.office', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 0),
       (29, 'BANK', 'Bank Maintenance', 'bank.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 1),
       (30, 'CHART_OF_ACCOUNT', 'Chart of Account', 'chart.of.account', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 2),
       (31, 'DISTRIBUTION_TEMPLATE_MAINTENANCE', 'Distribution Template Maintenance', 'distribution.template.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 3),
       (32, 'FINANCIAL_STATEMENT', 'Financial Statement', 'financial.statement', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 4),
       (33, 'GL_SOURCE_CODE', 'GL SOURCE CODE', 'gl.source.code', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 5),
       (34, 'SHIPVIA', 'Ship Via Maintenance', 'ship.via.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 6),
       (35, 'VENDOR', 'Vendor Maintenance', 'vendor.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 7),
       (36, 'VENDOR_GROUP', 'Vendor Group', 'vendor.group', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 7),
       (37, 'RESTRICTED_MASTER_CONTROL_FILES', 'RESTRICTED MASTER CONTROL FILES', 'restricted.master.control.files', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 8),
       (38, 'COMPANY', 'Company Maintenance', 'company.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 9),
       (39, 'DIVISION', 'Division Maintenance', 'division.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 10),
       (40, 'REGION', 'Region Maintenance', 'region.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 11),
       (41, 'STORE', 'Store Maintenance', 'region.maintenance', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 12),
       (42, 'VENDOR_TERM_CODE', 'Vendor Term Code', 'vendor.term.code', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 13),
       (43, 'AP_CONTROL', 'AP Control', 'account.payable.control', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 14),
       (44, 'PO_CONTROL', 'PO Control', 'po.control', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 15),
       (45, 'GL_CONTROL', 'GL Control', 'general.ledger.control', (SELECT id FROM area_type_domain WHERE value = 'MCF'), 16)
;

CREATE TABLE module_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(50) CHECK ( char_length(trim(value)) > 1)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    program           VARCHAR(50) CHECK ( char_length(trim(program)) > 1)            NOT NULL,
    menu_type_id      BIGINT REFERENCES menu_type_domain (id)                        NOT NULL,
    UNIQUE (value, program)
);

INSERT INTO module_type_domain (id, value, description, localization_code, program, menu_type_id)
VALUES (1, 'APADD', 'Add Invoices', 'add.invoices', 'APADD', (SELECT id FROM menu_type_domain WHERE value = 'INVOICE_MAINTENANCE')),
       (2, 'APSHO', 'Show Invoices', 'show.invoices', 'APSHO', (SELECT id FROM menu_type_domain WHERE value = 'INVOICE_MAINTENANCE')),
       (3, 'APCHG', 'AP Change Invoices', 'change.invoices','APCHG', (SELECT id FROM menu_type_domain WHERE value = 'INVOICE_MAINTENANCE')),
       (4, 'APDEL', 'Delete Invoices', 'delete.invoices', 'APDEL', (SELECT id FROM menu_type_domain WHERE value = 'INVOICE_MAINTENANCE')),

       (5, 'APCHECK', 'Print Checks', 'print.checks','APCHECK', (SELECT id FROM menu_type_domain WHERE value = 'CHECK_MAINTENANCE')),
       (6, 'APCHKRPT', 'Check Report', 'check.report','APCHKRPT', (SELECT id FROM menu_type_domain WHERE value = 'CHECK_MAINTENANCE')),
       (7, 'APCLEAR', 'Clear Checks', 'clear.checks', 'APCLEAR', (SELECT id FROM menu_type_domain WHERE value = 'CHECK_MAINTENANCE')),
       (8, 'APSEL', 'Select Invoices', 'select.invoices', 'APSEL', (SELECT id FROM menu_type_domain WHERE value = 'CHECK_MAINTENANCE')),
       (9, 'APPREVUE', 'Check Preview Rpt', 'check.preview.rpt', 'APPREVUE', (SELECT id FROM menu_type_domain WHERE value = 'CHECK_MAINTENANCE')),
       (10, 'APVOID', 'Void Checks', 'void.checks', 'APVOID', (SELECT id FROM menu_type_domain WHERE value = 'CHECK_MAINTENANCE')),

       (11, 'APAGERPT', 'Aging Report', 'aging.report', 'APAGERPT', (SELECT id FROM menu_type_domain WHERE value = 'AP_REPORTS')),
       (12, 'APRPT', 'AP Report','ap.report', 'APRPT', (SELECT id FROM menu_type_domain WHERE value = 'AP_REPORTS')),
       (13, 'CASHOUT', 'Cash Requirements', 'cash.requirements', 'CASHOUT', (SELECT id FROM menu_type_domain WHERE value = 'AP_REPORTS')),

       (14, 'APSTATUS', 'Vendor Statistics', 'vendor.statistics','APSTATUS', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (15, 'POADD','Add PO', 'add.po', 'POADD', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (16, 'POCHG', 'Change PO', 'change.po', 'POCHG', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (17, 'PODEL', 'Delete PO', 'delete.po', 'PODEL', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (18, 'POLST', 'List by PO', 'list.by.po', 'POLST', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (19, 'POPURGE','Purge PO Records', 'purge.po.records', 'POPURGE', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (20, 'POSHO', 'Inquiry', 'inquiry', 'POSHO', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (21, 'POINLOAD', 'Receive From PO', 'receive.from.po', 'POINLOAD', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (22, 'POUPDT', 'Update PO', 'update.po', 'POUPDT', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (23, 'SPOADD', 'Special Orders', 'special.orders', 'SPOADD', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (24, 'SPOLST', 'List Special Ord', 'list.special.ord', 'SPOLST', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (25, 'POCAN', 'Cancel PO', 'cancel.po', 'POCAN', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (26, 'POCOPY', 'Copy a PO', 'copy.a.po', 'POCOPY', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (27, 'INVORDMT', 'Allocate/Inq Special Orders and POs', 'allocate.inq.special.orders.and.pos', 'INVORDMT', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (28, 'INVCRED', 'Return Item for Credit to Vendor', 'return.item.for.credit.to.vendor', 'INVCRED', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (29, 'Inventory Availability', 'inventory.availability', 'inventory.availability', 'INVAVAIL', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (30, 'POSTAT', 'Change PO Status to Open', 'change.po.status.to.open', 'POSTAT', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (31, 'PODLST', 'List by Items', 'list.by.items', 'PODLST', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (32, 'POSTAT1', 'Change PO Status', 'change.po.status', 'POSTAT1', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (33, 'PODSQLST', 'List PO by Sequence #', 'list.po.by.sequence.#', 'PODSQLST', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (34, 'ITEMMNTS', 'Model Maintenance', 'model.maintenance', 'ITEMMNTS', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (35, 'VENDOR', 'Vendor Maintenance', 'vendor.maintenance', 'VENDOR', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),
       (36, 'PODETCHG', 'Adjust Receiving Quantities', 'adjust.receiving.quantities', 'PODETCHG', (SELECT id FROM menu_type_domain WHERE value = 'PO_MAINTENANCE')),

       (37, 'POREC', 'Enter Receiving', 'enter.receiving','POREC', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (38, 'PORECLST','List Receiving', 'list.receiving', 'PORECLST', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (39, 'PORECRPT', 'Receiving Rpt', 'receiving.rpt', 'PORECRPT', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (40, 'PORPT', 'PO Report', 'po.report', 'PORPT', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (41, 'POWRKSHT','Receiving Worksheet', 'receiving.worksheet', 'POWRKSHT', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (42, 'QUOTERPT','Quote Report', 'quote.report', 'QUOTERPT', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (43, 'VDRQUOTE', 'Vendor Quotes', 'vendor.quotes', 'VDRQUOTE', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (44, 'SPOPRT', 'Prt Special Ord', 'prt.special.ord', 'SPOPRT', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (45, 'STKRERDR', 'Stock Reorder', 'stock.reorder', 'STKRERDR', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (46, 'GETSTKLV', 'Update Stock Reorder Control File', 'update.stock.reorder.control.file', 'GETSTKLV', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (47, 'PINVBC', 'Receiver Report', 'receiver.report', 'PINVBC', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),
       (48, 'PINORDRT', 'Special Orders', 'special.orders', 'PINORDRT', (SELECT id FROM menu_type_domain WHERE value = 'PO_REPORTS')),

       (49, 'PARAMS', 'Control File Maintenance', 'control.file.maintenance', 'PARAMS', (SELECT id FROM menu_type_domain WHERE value = 'MASTER_CONTROL_FILE_MAINTENANCE')),

       (50, 'APGLRPT', 'G/L Analysis', 'gl.analysis', 'APGLRPT', (SELECT id FROM menu_type_domain WHERE value = 'ACCOUNT_INQUIRY_ANALYSIS')),

       (51, 'ADDCOMP', 'Add a Company Record', 'add.a.company.record','ADDCOMP', (SELECT id FROM menu_type_domain WHERE value = 'COMPANY')),
       (52, 'CHGCOMP', 'Change a Company Record', 'change.a.company.record','CHGCOMP', (SELECT id FROM menu_type_domain WHERE value = 'COMPANY')),
       (53, 'DELCOMP', 'Delete a Company Record', 'delete.a.company.record', 'DELCOMP', (SELECT id FROM menu_type_domain WHERE value = 'COMPANY')),
       (54, 'LSTCOMP', 'List all Company Records', 'list.all.company.records','LSTCMP', (SELECT id FROM menu_type_domain WHERE value = 'COMPANY')),
       (55, 'PRTCOMP', 'Print Company Report', 'print.company.report', 'PRTCOMP', (SELECT id FROM menu_type_domain WHERE value = 'COMPANY')),
       (56, 'SHOCOMP', 'Show a Company Record', 'show.a.company.record','SHOCOMP', (SELECT id FROM menu_type_domain WHERE value = 'COMPANY')),
       (57, 'SETSYS', 'Modify Company Areas', 'modify.company.areas', 'SETSYS', (SELECT id FROM menu_type_domain WHERE value = 'COMPANY')),

       (58, 'POPARAMS', 'PO Control', 'po.control','POPARAMS', (SELECT id FROM menu_type_domain WHERE value = 'PO_CONTROL')),

       (59, 'APLST', 'Vendor Invoices', 'vendor.invoices', 'APRPT', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR')),
       (60, 'ADDVEND', 'Add a New Vendor', 'add.a.new.vendor', 'ADDVEND', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR')),
       (61, 'CHGVEND', 'Change a Vendor', 'change.a.vendor', 'CHGVEND', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR')),
       (62, 'DELVEND', 'Delete a Vendor', 'delete.a.vendor', 'DELVEND', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR')),
       (63, 'LSTVEND', 'List all Vendors', 'list.all.vendors', 'LSTVEND', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR')),
       (64, 'PRTVEND', 'Print Vendor Report', 'print.vendor.report', 'PRTVEND', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR')),
       (65, 'DEFVEND', 'Set Default Vendor Profile', 'set.default.vendor.profile','DEFVEND', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR')),
       (66, 'SHOVEND', 'Show a Vendor', 'show.a.vendor','SHOVEND', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR')),

       (67, 'SHIPVIA', 'Enter/modify Ship Via', 'enter.modify.ship.via', 'SHIPVIA', (SELECT id FROM menu_type_domain WHERE value = 'SHIPVIA')),
       (68, 'SVCADD', 'Add a Ship Via Code', 'add.svc', 'SVCADD', (SELECT id FROM menu_type_domain WHERE value = 'SHIPVIA')),
       (69, 'SVCCHG', 'Change a Ship Via Code', 'change.svc', 'SVCCHG', (SELECT id FROM menu_type_domain WHERE value = 'SHIPVIA')),
       (70, 'SVCDEL', 'Delete a Ship Via Code', 'delete.svc', 'SVCDEL', (SELECT id FROM menu_type_domain WHERE value = 'SHIPVIA')),
       (71, 'SVCPRT', 'Print Ship Via Code', 'print.report.svc', 'SVCPRT', (SELECT id FROM menu_type_domain WHERE value = 'SHIPVIA')),
       (72, 'SVCSHW', 'Show a Ship Via List', 'show.svc', 'SVCSHW', (SELECT id FROM menu_type_domain WHERE value = 'SHIPVIA')),

       (73, 'GETVTERM', 'Vendor Term Code', 'vendor.term.code','GETVTERM', (SELECT id FROM menu_type_domain WHERE value = 'VENDOR_TERM_CODE')),

       (74, 'APPURGE', 'Purge AP Records','purge.ap.records', 'APPURGE', (SELECT id FROM menu_type_domain WHERE value = 'AP_CONTROL')),
       (75, 'APPARAMS', 'AP Control', 'ap.control','APPARAMS', (SELECT id FROM menu_type_domain WHERE value = 'AP_CONTROL')),

       (76, 'ADDACCT', 'Add a G/L Account', 'add.a.gl.account', 'ADDACCT', (SELECT id FROM menu_type_domain WHERE value = 'CHART_OF_ACCOUNT')),
       (77, 'CHGACCT', 'Change a G/L Account', 'chg.a.gl.account', 'CHGACCT', (SELECT id FROM menu_type_domain WHERE value = 'CHART_OF_ACCOUNT')),
       (78, 'DELACCT', 'Delete a G/L Account', 'delete.a.gl.account','DELACCT', (SELECT id FROM menu_type_domain WHERE value = 'CHART_OF_ACCOUNT')),
       (79, 'LSTACCT', 'List all G/L Accounts', 'list.all.gl.accounts', 'LSTACCT', (SELECT id FROM menu_type_domain WHERE value = 'CHART_OF_ACCOUNT')),
       (80, 'PRTACCT', 'Print Chart of Accounts', 'print.chart.of.accounts','PRTACCT', (SELECT id FROM menu_type_domain WHERE value = 'CHART_OF_ACCOUNT')),
       (81, 'CPYACCT', 'Reproduce a G/L Account', 'reproduce.a.gl.account','CPYACCT', (SELECT id FROM menu_type_domain WHERE value = 'CHART_OF_ACCOUNT')),
       (82, 'SHOACCT', 'Show a G/L Account', 'show.a.gl.account','SHOACCT', (SELECT id FROM menu_type_domain WHERE value = 'CHART_OF_ACCOUNT')),

       (83, 'ADDBANK', 'Add a New Bank', 'add.a.new.bank','ADDBANK', (SELECT id FROM menu_type_domain WHERE value = 'BANK')),
       (84, 'CHGBANK', 'Change a Bank', 'change.a.bank', 'CHGBANK', (SELECT id FROM menu_type_domain WHERE value = 'BANK')),
       (85, 'DELBANK', 'Delete a Bank', 'delete.a.bank', 'DELBANK', (SELECT id FROM menu_type_domain WHERE value = 'BANK')),
       (86, 'LSTBANK', 'List All Banks', 'list.all.banks','LSTBANK', (SELECT id FROM menu_type_domain WHERE value = 'BANK')),
       (87, 'PRTBANK', 'Print Bank Report', 'print.bank.report', 'PRTBANK', (SELECT id FROM menu_type_domain WHERE value = 'BANK')),
       (88, 'SHOBANK', 'Show a Bank', 'show.a.bank', 'SHOBANK', (SELECT id FROM menu_type_domain WHERE value = 'BANK')),

       (89, 'GLPARAMS', 'GL Control', 'gl.control','GLPARAMS', (SELECT id FROM menu_type_domain WHERE value = 'GL_CONTROL')),

       (90, 'ADDLAY', 'Add a Statement Layout', 'add.a.statement.layout', 'ADDLAY', (SELECT id FROM menu_type_domain WHERE value = 'FINANCIAL_STATEMENT')),
       (91, 'CHGLAY', 'Change a Statement Layout', 'change.a.statement.layout', 'CHGLAY', (SELECT id FROM menu_type_domain WHERE value = 'FINANCIAL_STATEMENT')),
       (92, 'DELLAY', 'Delete a Statement Layout', 'delete.a.statement.layout','DELLAY', (SELECT id FROM menu_type_domain WHERE value = 'FINANCIAL_STATEMENT')),
       (93, 'FORMLAY', 'Format a Sample Statement', 'format.a.sample.statement', 'FORMLAY', (SELECT id FROM menu_type_domain WHERE value = 'FINANCIAL_STATEMENT')),
       (94, 'PRTLAY', 'Print a Statement Layout', 'print.a.statement.layout', 'PRTLAY', (SELECT id FROM menu_type_domain WHERE value = 'FINANCIAL_STATEMENT')),
       (95, 'CPYLAY', 'Reproduce a Statement Layout', 'reproduce.a.statement.layout', 'CPYLAY', (SELECT id FROM menu_type_domain WHERE value = 'FINANCIAL_STATEMENT')),
       (96, 'SHOLAY', 'Show a Statement Layout', 'show.a.statement.layout', 'SHOLAY', (SELECT id FROM menu_type_domain WHERE value = 'FINANCIAL_STATEMENT')),

       (97, 'ADDGLCOD', 'Add a New G/L Code', 'add.a.new.gl.code', 'ADDGLCOD', (SELECT id FROM menu_type_domain WHERE value = 'GL_SOURCE_CODE')),
       (98, 'CHGGLCOD', 'Change a G/L Code', 'change.a.gl.code', 'CHGGLCOD', (SELECT id FROM menu_type_domain WHERE value = 'GL_SOURCE_CODE')),
       (99, 'DELGLCOD', 'Delete a G/L Code', 'delete.a.gl.code','DELGLCOD', (SELECT id FROM menu_type_domain WHERE value = 'GL_SOURCE_CODE')),
       (100, 'LSTGLCOD', 'List all G/L Codes', 'list.all.gl.codes', 'LSTGLCOD', (SELECT id FROM menu_type_domain WHERE value = 'GL_SOURCE_CODE')),
       (101, 'PRTGLCOD', 'Print G/L Code Report', 'print.gl.code.report','PRTGLCOD', (SELECT id FROM menu_type_domain WHERE value = 'GL_SOURCE_CODE')),
       (102, 'SHOGLCOD', 'Show a G/L Code', 'show.a.gl.code', 'SHOGLCOD', (SELECT id FROM menu_type_domain WHERE value = 'GL_SOURCE_CODE')),

       (103, 'ADDAPDST', 'Add a Distribution Template', 'add.a.distribution.template','ADDAPDST', (SELECT id FROM menu_type_domain WHERE value = 'DISTRIBUTION_TEMPLATE_MAINTENANCE')),
       (104, 'CHGAPDST', 'Change a Distribution Template', 'change.a.distribution.template','CHGAPDST', (SELECT id FROM menu_type_domain WHERE value = 'DISTRIBUTION_TEMPLATE_MAINTENANCE')),
       (105, 'DELAPDST', 'Delete a Distribution Template', 'delete.a.distribution.template', 'DELAPDST', (SELECT id FROM menu_type_domain WHERE value = 'DISTRIBUTION_TEMPLATE_MAINTENANCE')),
       (106, 'SHOAPDST', 'Show a Distribution Template', 'show.a.distribution.template', 'SHOAPDST', (SELECT id FROM menu_type_domain WHERE value = 'DISTRIBUTION_TEMPLATE_MAINTENANCE')),
       (107, 'PRTAPDST', 'Print a Distribution Template', 'print.a.distribution.template', 'PRTAPDST', (SELECT id FROM menu_type_domain WHERE value = 'DISTRIBUTION_TEMPLATE_MAINTENANCE'))
;

CREATE TABLE area
(
    uu_row_id      UUID        DEFAULT uuid_generate_v1()    NOT NULL,
    time_created   TIMESTAMPTZ DEFAULT clock_timestamp()     NOT NULL,
    time_updated   TIMESTAMPTZ DEFAULT clock_timestamp()     NOT NULL,
    area_type_id   INTEGER REFERENCES area_type_domain(id)   NOT NULL,
    company_id     BIGINT REFERENCES company(id)             NOT NULL,
    UNIQUE (company_id, area_type_id)
);

CREATE TRIGGER update_area_trg
BEFORE UPDATE
   ON area
FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE module
(
    uu_row_id      UUID        DEFAULT uuid_generate_v1()     NOT NULL,
    time_created   TIMESTAMPTZ DEFAULT clock_timestamp()      NOT NULL,
    time_updated   TIMESTAMPTZ DEFAULT clock_timestamp()      NOT NULL,
    level          INTEGER CHECK (level > -1 and level < 100) NOT NULL,
    module_type_id BIGINT REFERENCES module_type_domain (id)  NOT NULL,
    company_id     BIGINT REFERENCES company (id)             NOT NULL,
    UNIQUE (company_id, module_type_id)
);

CREATE TRIGGER update_module_trg
BEFORE UPDATE
   ON module
FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
-- end user tables
