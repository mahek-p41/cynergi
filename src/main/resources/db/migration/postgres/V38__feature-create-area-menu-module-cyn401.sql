-- being domain tables
CREATE TABLE area_type_domain
(
    id                 INTEGER                                                        NOT NULL PRIMARY KEY,
    value              VARCHAR(50) CHECK ( char_length(trim(value)) > 1)              NOT NULL,
    description        VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code  VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
COMMENT ON TABLE area_type_domain IS 'Domain table contains the individual areas/systems available for a company to implement. i.e. PO, AP, GL';

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

INSERT INTO menu_type_domain (id,value, description, localization_code, area_type_id, order_number)
VALUES (1,'ACCOUNTS PAYABLE', 'Account Payable', 'account.payable', 1,0),
       (2,'BANK RECONCILIATION', 'Bank Reconciliation', 'bank.reconciliation', 2,0),
       (3,'GENERAL LEDGER', 'General Ledger', 'general.ledger', 3,0),
       (4,'PURCHASE ORDER', 'Purchase Order', 'purchase.order', 4,0),
       (5,'INVOICE MAINTENANCE', 'AP Invoice Maintenance', 'account.payable.invoice.maintenance', 1,1),
       (6,'CHECK MAINTENANCE', 'AP Check Maintenance', 'account.payable.check.maintenance', 1,3),
       (7,'AP REPORTS', 'AP Reports', 'account.payable.reports', 1,4),
       (8,'PO MAINTENANCE', 'PO Maintenance', 'purchase.order.maintenance', 4,1),
       (9,'PO REPORTS', 'PO Reports', 'purchase.order.reports', 4,2),
       (10,'MASTER CONTROL FILE MAINTENANCE', 'MCF Maintenance', 'master.control.file.maintenance', 5,0),
       (11,'STORE DEPOSIT MAINTENANCE', 'Store Deposit Maintenance', 'store.deposit.maintenance', 2,1),
       (13,'CLEAR OUTSTANDING ITEMS', 'Outstanding Items Maintenance', 'outstanding.items.maintenance', 2,3),
       (14,'BANK TRANSACTIONS MAINTENANCE', 'Bank Transactions Maintenance', 'bank.transactions.maintenance', 2,5),
       (15,'RECONCILE BANK ACCOUNT', 'Reconcile Bank Account', 'reconcile.bank.account',2,4),
       (16,'ACCOUNT INQUIRY/ANALYSIS', 'GL Inquiry/Analysis', 'general.ledger.inquiry.analysis', 3,1),
       (17,'JOURNAL ENTRY PROCESSING', 'Journal Entry Processing', 'journal.entry.processing', 3,2),
       (18,'GL REPORTS', 'GL Reports', 'general.ledger.reports', 3,6),
       (19,'MONTH END PROCESSING', 'GL End of Month Processing', 'general.ledger.end.of.month.processing', 3,8),
       (20,'UTILITIES', 'GL Utilities', 'general.ledger.utilities', 3,9),
       (21,'COMPANY', 'Company Maintenance', 'company.maintenance', 5,8),
       (22,'REGION', 'Region Maintenance', 'region', 5,10),
       (23,'DIVISION', 'Division Maintenance', 'division', 5,9),
       (24,'PO CONTROL', 'PO Control', 'po.control', 5,13),
       (25,'VENDOR', 'Vendor Maintenance', 'vendor.maintenance', 5,7),
       (26,'SHIP VIA', 'Ship Via Maintenance', 'ship.via.maintenance', 5,6),
       (27,'VENDOR TERM CODE', 'Vendor Term Code', 'vendor.term.code', 5,8),
       (28,'AP CONTROL', 'AP Control', 'account.payable.control', 5,11),
       (29,'CHART OF ACCOUNT', 'Chart of Account', 'chart.of.account', 5,2),
       (30,'BANK', 'Bank Maintenance', 'bank.maintenance', 5,1),
       (31,'GL CONTROL', 'GL Control', 'general.ledger.control', 5,12),
       (32,'FINANCIAL STATEMENT', 'Financial Statement', 'financial.statement', 5,4),
       (33,'PO REPORT (w/Export)', 'PO Report (w/Export)', 'po.report.(w/Export)', 4,3),
       (34,'STOCK REORDER', 'STOCK REORDER REPORT', 'stock.reorder.report', 4,4),
       (35,'RECEIVER REPORT', 'RECEIVER REPORT', 'receiver.report', 4,5),
       (36,'RECEIVING WORKSHEET', 'RECEIVING WORKSHEET', 'receiving.worksheet', 4,6),
       (37,'SPECIAL ORDERS', 'SPECIAL ORDERS', 'special.orders', 4,7),
       (38,'GL SOURCE CODE', 'GL SOURCE CODE', 'gl.source.code', 5,5),
       (39,'RESTRICTED MASTER CONTROL FILES', 'RESTRICTED MASTER CONTROL FILES', 'restricted.master.control.files', 5,7),
       (40,'HOME OFFICE', 'HOME OFFICE', 'home.office', 5,0),
       (41,'RECURRING INVOICE MAINTENANCE', 'Recurring Invoice Maintenance', 'recurring.invoice.maintenance', 1,2),
       (42,'MONTH END', 'Account Payable Month End', 'account.payable.month.end', 1,5),
       (43,'DISTRIBUTION TEMPLATE MAINTENANCE', 'Distribution Template Maintenance', 'distribution.template.maintenance', 5,3),
       (44,'SEND STORE DEPOSIT TO BANK REC', 'Send Store Deposit to Bank Rec', 'send.store.deposit.to.bank.rec', 2,2);


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
VALUES (1, 'APADD', 'Add Invoices', 'add.invoices', 'APADD', 5),
       (2, 'APAGERPT', 'Aging Report', 'aging.report', 'APAGERPT', 7),
       (3, 'APCHECK', 'Print Checks', 'print.checks','APCHECK', 6),
       (4, 'APCHG', 'AP Change Invoices', 'change.invoices','APCHG', 5),
       (5, 'APCHKRPT', 'Check Report', 'check.report','APCHKRPT', 6),
       (6, 'APCLEAR', 'Clear Checks', 'clear.checks', 'APCLEAR', 6),
       (7, 'APDEL', 'Delete Invoices', 'delete.invoices', 'APDEL', 5),
       (8, 'APGLRPT', 'G/L Analysis', 'gl.analysis', 'APGLRPT', 17),
       (9, 'APLST', 'Vendor Invoices', 'vendor.invoices', 'APRPT', 26),
       (10, 'APPREVUE', 'Check Preview Rpt', 'check.preview.rpt', 'APPREVUE', 6),
       (11, 'APPURGE', 'Purge AP Records','purge.ap.records', 'APPURGE', 29),
       (12, 'APRPT', 'AP Report','ap.report', 'APRPT', 7),
       (13, 'APSEL', 'Select Invoices', 'select.invoices', 'APSEL', 6),
       (14, 'APSHO','Show Invoices', 'show.invoices', 'APSHO', 5),
       (15, 'APSTATUS', 'Vendor Statistics', 'vendor.statistics','APSTATUS', 8),
       (17, 'APVOID', 'Void Checks', 'void.checks', 'APVOID', 6),
       (18, 'CASHOUT', 'Cash Requirements', 'cash.requirements', 'CASHOUT', 7),
       (19, 'POADD','Add PO', 'add.po', 'POADD', 8),
       (20, 'POCHG', 'Change PO', 'change.po', 'POCHG', 8),
       (21, 'PODEL', 'Delete PO', 'delete.po', 'PODEL', 8),
       (22, 'POINLOAD', 'Receive From PO', 'receive.from.po', 'POINLOAD', 8),
       (23, 'POLST', 'List by PO', 'list.by.po', 'POLST', 8),
       (24, 'PARAMS', 'Control File Maintenance', 'control.file.maintenance', 'PARAMS', 10),
       (25, 'POPURGE','Purge PO Records', 'purge.po.records', 'POPURGE', 8),
       (26, 'POREC', 'Enter Receiving', 'enter.receiving','POREC', 9),
       (27, 'PORECLST','List Receiving', 'list.receiving', 'PORECLST', 9),
       (28, 'PORECRPT', 'Receiving Rpt', 'receiving.rpt', 'PORECRPT', 9),
       (29, 'PORPT', 'PO Report', 'po.report', 'PORPT', 9),
       (30, 'POSHO', 'Inquiry', 'inquiry', 'POSHO', 8),
       (31, 'POUPDT', 'Update PO', 'update.po', 'POUPDT', 8),
       (32, 'POWRKSHT','Receiving Worksheet', 'receiving.worksheet', 'POWRKSHT', 9),
       (33, 'QUOTERPT','Quote Report', 'quote.report', 'QUOTERPT', 9),
       (34, 'VDRQUOTE', 'Vendor Quotes', 'vendor.quotes', 'VDRQUOTE', 9),
       (35, 'SPOADD', 'Special Orders', 'special.orders', 'SPOADD', 8),
       (36, 'SPOLST', 'List Special Ord', 'list.special.ord', 'SPOLST', 8),
       (37, 'SPOPRT', 'Prt Special Ord', 'prt.special.ord', 'SPOPRT', 9),
       (38, 'POCAN', 'Cancel PO', 'cancel.po', 'POCAN', 8),
       (39, 'POCOPY', 'Copy a PO', 'copy.a.po', 'POCOPY', 8),
       (40, 'INVORDMT', 'Allocate/Inq Special Orders and POs', 'allocate/inq.special.orders.and.pos', 'INVORDMT', 8),
       (41, 'INVCRED', 'Return Item for Credit to Vendor', 'return.item.for.credit.to.vendor', 'INVCRED', 8),
       (42, 'PODLST', 'List by Items', 'list.by.items', 'PODLST', 8),
       (43, 'Inventory Availability', 'inventory.availability', 'inventory.availability', 'INVAVAIL', 8),
       (44, 'POSTAT', 'Change PO Status to Open', 'change.po.status.to.open', 'POSTAT', 8),
       (45, 'POSTAT1', 'Change PO Status', 'change.po.status', 'POSTAT1', 8),
       (46, 'PODETCHG', 'Adjust Receiving Quantities', 'adjust.receiving.quantities', 'PODETCHG', 8),
       (47, 'PODSQLST', 'List PO by Sequence #', 'list.po.by.sequence.#', 'PODSQLST', 8),
       (48, 'ITEMMNTS', 'Model Maintenance', 'model.maintenance', 'ITEMMNTS', 8),
       (49, 'VENDOR', 'Vendor Maintenance', 'vendor.maintenance', 'VENDOR', 8),
       (50, 'STKRERDR', 'Stock Reorder', 'stock.reorder', 'STKRERDR', 9),
       (51, 'GETSTKLV', 'Update Stock Reorder Control File', 'update.stock.reorder.control.file', 'GETSTKLV', 9),
       (52, 'PINVBC', 'Receiver Report', 'receiver.report', 'PINVBC', 9),
       (53, 'PINORDRT', 'Special Orders', 'special.orders', 'PINORDRT', 9),
       (54, 'ADDBANK', 'Add a New Bank', 'add.a.new.bank','ADDBANK', 30),
       (55, 'CHGBANK', 'Change a Bank', 'change.a.bank', 'CHGBANK', 30),
       (56, 'DELBANK', 'Delete a Bank', 'delete.a.bank', 'DELBANK',30),
       (57, 'LSTBANK', 'List All Banks', 'list.all.banks','LSTBANK', 30),
       (58, 'PRTBANK', 'Print Bank Report', 'print.bank.report', 'PRTBANK',30),
       (59, 'SHOBANK', 'Show a Bank', 'show.a.bank', 'SHOBANK',30),
       (60, 'ADDACCT', 'Add a G/L Account', 'add.a.g/l.account', 'ADDACCT',29),
       (61, 'CHGACCT', 'Change a G/L Account', 'chg.a.g/l.account', 'CHGACCT',29),
       (62, 'DELACCT', 'Delete a G/L Account', 'delete.a.g/l.account','DELACCT', 29),
       (63, 'LSTACCT', 'List all G/L Accounts', 'list.all.g/l.accounts', 'LSTACCT',29),
       (64, 'PRTACCT', 'Print Chart of Accounts', 'print.chart.of.accounts','PRTACCT', 29),
       (65, 'CPYACCT', 'Reproduce a G/L Account', 'reproduce.a.g/l.account','CPYACCT', 29),
       (66, 'SHOACCT', 'Show a G/L Account', 'show.a.g/l.account','SHOACCT', 29),
       (67, 'ADDAPDST', 'Add a Distribution Template', 'add.a.distribution.template','ADDAPDST', 43),
       (68, 'CHGAPDST', 'Change a Distribution Template', 'change.a.distribution.template','CHGAPDST', 43),
       (69, 'DELAPDST', 'Delete a Distribution Template', 'delete.a.distribution.template', 'DELAPDST',43),
       (70, 'SHOAPDST', 'Show a Distribution Template', 'show.a.distribution.template', 'SHOAPDST',43),
       (71, 'PRTAPDST', 'Print a Distribution Template', 'print.a.distribution.template', 'PRTAPDST',43),
       (72, 'ADDLAY', 'Add a Statement Layout', 'add.a.statement.layout', 'ADDLAY',32),
       (73, 'CHGLAY', 'Change a Statement Layout', 'change.a.statement.layout', 'CHGLAY',32),
       (74, 'DELLAY', 'Delete a Statement Layout', 'delete.a.statement.layout','DELLAY', 32),
       (75, 'FORMLAY', 'Format a Sample Statement', 'format.a.sample.statement', 'FORMLAY',32),
       (76, 'PRTLAY', 'Print a Statement Layout', 'print.a.statement.layout', 'PRTLAY',32),
       (77, 'CPYLAY', 'Reproduce a Statement Layout', 'reproduce.a.statement.layout', 'CPYLAY',32),
       (78, 'SHOLAY', 'Show a Statement Layout', 'show.a.statement.layout', 'SHOLAY',32),
       (79, 'ADDGLCOD', 'Add a New G/L Code', 'add.a.new.g/l.code', 'ADDGLCOD', 38),
       (80, 'CHGGLCOD', 'Change a G/L Code', 'change.a.g/l.code', 'CHGGLCOD',38),
       (81, 'DELGLCOD', 'Delete a G/L Code', 'delete.a.g/l.code','DELGLCOD', 38),
       (82, 'LSTGLCOD', 'List all G/L Codes', 'list.all.g/l.codes', 'LSTGLCOD',38),
       (83, 'PRTGLCOD', 'Print G/L Code Report', 'print.g/l.code.report','PRTGLCOD', 38),
       (84, 'SHOGLCOD', 'Show a G/L Code', 'show.a.g/l.code', 'SHOGLCOD', 38),
       (85, 'SHIPVIA', 'Enter/modify Ship Via', 'enter/modify.ship.via', 'SHIPVIA',26),
       (86, 'ADDVEND', 'Add a New Vendor', 'add.a.new.vendor', 'ADDVEND',25),
       (87, 'CHGVEND', 'Change a Vendor', 'change.a.vendor', 'CHGVEND',25),
       (88, 'DELVEND', 'Delete a Vendor', 'delete.a.vendor', 'DELVEND',25),
       (89, 'LSTVEND', 'List all Vendors', 'list.all.vendors', 'LSTVEND',25),
       (90, 'PRTVEND', 'Print Vendor Report', 'print.vendor.report', 'PRTVEND',25),
       (91, 'DEFVEND', 'Set Default Vendor Profile', 'set.default.vendor.profile','DEFVEND', 25),
       (92, 'SHOVEND', 'Show a Vendor', 'show.a.vendor','SHOVEND',25),
       (93, 'GETVTERM', 'Vendor Term Code', 'vendor.term.code','GETVTERM',27),
       (94, 'ADDCOMP', 'Add a Company Record', 'add.a.company.record','ADDCOMP', 21),
       (95, 'CHGCOMP', 'Change a Company Record', 'change.a.company.record','CHGCOMP', 21),
       (96, 'DELCOMP', 'Delete a Company Record', 'delete.a.company.record', 'DELCOMP',21),
       (97, 'LSTCOMP', 'List all Company Records', 'list.all.company.records','LSTCMP', 21),
       (98, 'PRTCOMP', 'Print Company Report', 'print.company.report', 'PRTCOMP',21),
       (99, 'SHOCOMP', 'Show a Company Record', 'show.a.company.record','SHOCOMP', 21),
       (100, 'SETSYS', 'Modify Company Areas', 'modify.company.areas', 'SETSYS',21),
       (101, 'APPARAMS', 'AP Control', 'ap.control','APPARAMS', 28),
       (102, 'GLPARAMS', 'GL Control', 'gl.control','GLPARAMS', 31),
       (103, 'POPARAMS', 'PO Control', 'po.control','POPARAMS', 24),
       (104, 'SVCADD', 'Add a Ship Via Code', 'add.svc', 'SVCADD', 27),
       (105, 'SVCCHG', 'Change a Ship Via Code', 'change.svc', 'SVCCHG', 27),
       (106, 'SVCDEL', 'Delete a Ship Via Code', 'delete.svc', 'SVCDEL', 27),
       (107, 'SVCPRT', 'Print Ship Via Code', 'print.report.svc', 'SVCPRT', 27),
       (108, 'SVCSHW', 'Show a Ship Via Code', 'show.svc', 'SVCSHW', 27);


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





