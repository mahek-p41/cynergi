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
    UNIQUE (area_type_id, value)
);

INSERT INTO menu_type_domain (id,value, description, localization_code, area_type_id)
VALUES (1,'ACCOUNTS PAYABLE', 'Account Payable Menu', 'account.payable.menu', 1),
       (2,'BANK RECONCILIATION', 'Bank Reconciliation Menu', 'bank.reconciliation.menu', 2),
       (3,'GENERAL LEDGER', 'General Ledger Menu', 'general.ledger.menu', 3),
       (4,'PURCHASE ORDER', 'Purchase Order Menu', 'purchase.order.menu', 4),
       (5,'INVOICE MAINTENANCE', 'Account Payable Invoice Maintenance', 'account.payable.invoice.maintenance', 1),
       (6,'CHECK MAINTENANCE', 'Account Payable Check Maintenance', 'account.payable.check.maintenance', 1),
       (7,'REPORTS MENU', 'Accounts Payable Reports Menu', 'account.payable.reports.menu', 1),
       (8,'PO MAINTENANCE', 'Purchase Order Maintenance', 'purchase.order.maintenance', 4),
       (9,'PO REPORTS', 'Purchase Order Reports', 'purchase.order.reports', 4),
       (10,'MASTER CONTROL FILE MAINTENANCE', 'Master Control File Maintenance', 'master.control.file.maintenance', 5),
       (11,'STORE DEPOSIT MAINTENANCE', 'Store Deposit Maintenance', 'store.deposit.maintenance', 2),
       (12,'SEND STORE DEPOSIT TO BANK REC', 'Complete Store Deposit Process With Bank ', 'complete.store.deposit.process.with.bank', 2),
       (13,'CLEAR OUTSTANDING ITEMS', 'Outstanding Items Maintenance', 'outstanding.items.maintenance', 2),
       (14,'BANK TRANSACTIONS MAINTENANCE', 'Bank Transactions Maintenance', 'bank.transactions.maintenance', 2),
       (15,'RECONCILE BANK ACCOUNT', 'Reconcile Bank Account', 'reconcile.bank.account',2),
       (16,'BANK MAINTENANCE', 'Bank Maintenance', 'bank.maintenance', 2),
       (17, 'ACCOUNT INQUIRY/ANALYSIS', 'General Ledger Inquiry/Analysis', 'general.ledger.inquiry.analysis', 3),
       (18,'JOURNAL ENTRY PROCESSING', 'Journal Entry Processing', 'journal.entry.processing', 3),
       (19,'GL REPORTS', 'General Ledger Reports', 'general.ledger.reports', 3),
       (20,'MONTH END PROCESSING', 'General Ledger End of Month Processing', 'general.ledger.end.of.month.processing', 3),
       (21,'UTILITIES', 'General Ledger Utilities', 'general.ledger.utilities', 3),
       (22,'COMPANY', 'Company Maintenance Menu', 'company.maintenance.menu', 5),
       (23,'REGION', 'Region Menu', 'region.menu', 5),
       (24,'DIVISION', 'Division Menu', 'division.menu', 5),
       (25,'PO CONTROL', 'PO Control Menu', 'po.control.menu', 5),
       (26,'VENDOR', 'Vendor Maintenance', 'vendor.maintenance', 5),
       (27,'SHIP VIA', 'Ship Via Maintenance', 'ship.via.maintenance', 5),
       (28,'VENDOR TERM CODE', 'Vendor Term Code', 'vendor.term.code', 5),
       (29,'AP CONTROL', 'Account Payable Control', 'account.payable.control', 5),
       (30,'CHART OF ACCOUNT', 'Chart of Account', 'chart.of.account', 5),
       (31,'BANK', 'Bank Maintenance', 'bank.maintenance', 5),
       (32,'GL CONTROL', 'General Ledger Control', 'general.ledger.control', 5),
       (33,'FINANCIAL STATEMENT', 'Financial Statement', 'financial.statement', 5);

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
       (2, 'APAGERPT', 'Aging Report', 'aging.report', 'APAGERPT', 1),
       (3, 'APCHECK', 'Print Checks', 'print.checks','APCHECK', 6),
       (4, 'APCHG', 'AP Change Invoices', 'change.invoices','APCHG', 5),
       (5, 'APCHKRPT', 'Check Report', 'check.report','APCHKRPT', 6),
       (6, 'APCLEAR', 'Clear Checks', 'clear.checks', 'APCLEAR', 6),
       (7, 'APDEL', 'Delete Invoices', 'delete.invoices', 'APDEL', 5),
       (8, 'APGLRPT', 'G/L Analysis', 'gl.analysis', 'APGLRPT', 3),
       (9, 'APLST', 'Vendor Invoices', 'vendor.invoices', 'APRPT', 1),
       (10, 'APPREVUE', 'Check Preview Rpt', 'check.preview.rpt', 'APPREVUE', 6),
       (11, 'APPURGE', 'Purge AP Records','purge.ap.records', 'APPURGE', 1),
       (12, 'APRPT', 'AP Report','ap.report', 'APRPT', 7),
       (13, 'APSEL', 'Select Invoices', 'select.invoices', 'APSEL', 6),
       (14, 'APSHO','Show Invoices', 'show.invoices', 'APSHO', 5),
       (15, 'APSTATUS', 'Vendor Status', 'vendor.status','APSTATUS', 5),
       (17, 'APVOID', 'Void Checks', 'void.checks', 'APVOID', 6),
       (18, 'CASHOUT', 'Cash Requirements', 'cash.requirements', 'CASHOUT', 7),
       (19, 'POADD','Add PO', 'add.po', 'POADD', 8),
       (20, 'POCHG', 'Change PO', 'change.po', 'POCHG', 8),
       (21, 'PODEL', 'Delete PO', 'delete.po', 'PODEL', 8),
       (22, 'POINLOAD', 'Receive From PO', 'receive.from.po', 'POINLOAD', 8),
       (23, 'POLST', 'List PO', 'list.po', 'POLST', 8),
       (24, 'PARAMS', 'Control File Maintenance', 'control.file.maintenance', 'PARAMS', 10),
       (25, 'POPURGE','Purge PO Records', 'purge.po.records', 'POPURGE', 8),
       (26, 'POREC', 'Enter Receiving', 'enter.receiving','POREC', 9),
       (27, 'PORECLST','List Receiving', 'list.receiving', 'PORECLST', 9),
       (28, 'PORECRPT', 'Receiving Rpt', 'receiving.rpt', 'PORECRPT', 9),
       (29, 'PORPT', 'PO Report', 'po.report', 'PORPT', 9),
       (30, 'POSHO', 'Show PO', 'show.po', 'POSHO', 8),
       (31, 'POUPDT', 'Update PO', 'update.po', 'POUPDT', 8),
       (32, 'POWRKSHT','Recv Worksheets', 'recv.worksheets', 'POWRKSHT', 9),
       (33, 'QUOTERPT','Quote Report', 'quote.report', 'QUOTERPT', 9),
       (34, 'VDRQUOTE', 'Vendor Quotes', 'vendor.quotes', 'VDRQUOTE', 9),
       (35, 'SPOADD', 'Special Orders', 'special.orders', 'SPOADD', 8),
       (36, 'SPOLST', 'List Special Ord', 'list.special.ord', 'SPOLST', 8),
       (37, 'SPOPRT', 'Prt Special Ord', 'prt.special.ord', 'SPOPRT', 9),
       (38, 'POCAN', 'Cancel PO', 'cancel.po', 'POCAN', 8),
       (39, 'POCOPY', 'Copy a PO', 'copy.a.po', 'POCOPY', 8);

CREATE TABLE area
(
    id             BIGSERIAL                                 NOT NULL PRIMARY KEY,
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
    id             BIGSERIAL                                  NOT NULL PRIMARY KEY,
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





