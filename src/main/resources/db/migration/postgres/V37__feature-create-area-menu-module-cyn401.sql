-- being domain tables
CREATE TABLE area_type_domain
(
    id                 INTEGER                                                        NOT NULL PRIMARY KEY,
    value              VARCHAR(50) CHECK ( char_length(trim(value)) > 1)              NOT NULL,
    description        VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
COMMENT ON TABLE area_type_domain IS 'Domain table contains the individual areas/systems available for a company to implement. i.e. PO, AP, GL';

INSERT INTO area_type_domain (id, value, description, localization_code)
VALUES (1, 'AP', 'ACCOUNT PAYABLE', 'account.payable'),
       (2, 'BR', 'BANK RECONCILIATION', 'bank.reconciliation'),
       (3, 'GL', 'GENERAL LEDGER', 'general.ledger'),
       (4, 'PO', 'PURCHASE ORDER', 'purchase.order'),
       (5, 'MCF', 'Master Control Files', 'master.control.files');


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
VALUES (1,'ACCOUNTS PAYABLE', 'Account Payable Menu', 'account.payable.menu',1),
       (2,'BANK RECONCILIATION', 'Bank Reconciliation Menu', 'bank.reconciliation.menu',2),
       (3,'GENERAL LEDGER', 'General Ledger Menu', 'general.ledger.menu',3),
       (4,'PURCHASE ORDER', 'Purchase Order Menu', 'purchase.order.menu',4),
       (5,'INVOICE MAINTENANCE', 'Account Payable Invoice Maintenance', 'account.payable.invoice.maintenance',1),
       (6,'CHECK MAINTENANCE', 'Account Payable Check Maintenance', 'account.payable.check.maintenance',1),
       (7,'REPORTS MENU', 'Accounts Payable Reports Menu', 'account.payable.reports.menu',1),
       (8,'PO MAINTENANCE', 'Purchase Order Maintenance', 'purchase.order.maintenance',4),
       (9,'PO REPORTS', 'Purchase Order Reports', 'purchase.order.reports',4),
       (10,'MASTER CONTROL FILE MAINTENANCE', 'Master Control File Maintenance', 'master.control.file.maintenance',5),
       (11,'STORE DEPOSIT MAINTENANCE', 'Store Deposit Maintenance', 'store.deposit.maintenance',2),
       (12,'SEND STORE DEPOSIT TO BANK REC', 'Complete Store Deposit Process With Bank ', 'complete.store.deposit.process.with.bank',2),
       (13,'CLEAR OUTSTANDING ITEMS', 'Outstanding Items Maintenance', 'outstanding.items.maintenance',2),
       (14,'BANK TRANSACTIONS MAINTENANCE', 'Bank Transactions Maintenance', 'bank.transactions.maintenance',2),
       (15,'RECONCILE BANK ACCOUNT', 'Reconcile Bank Account', 'reconcile.bank.account',2),
       (16,'BANK MAINTENANCE', 'bank.maintenance', 'bank.maintenance',2),
       (17,'GENERAL LEDGER', 'General Ledger', 'general.ledger',3),
       (18,'ACCOUNT INQUIRY/ANALYSIS', 'General Ledger Inquiry/Analysis', 'general.ledger.inquiry.analysis',3),
       (19,'JOURNAL ENTRY PROCESSING', 'journal entry processing', 'journal.entry.processing',3),
       (20,'GL REPORTS', 'General Ledger Reports', 'general.ledger.reports',3),
       (21,'MONTH END PROCESSING', 'General Ledger End of Month Processing', 'general.ledger.end.of.month.processing',3),
       (22,'UTILITIES', 'General Ledger Utilities', 'general.ledger.utilities',3),
       (23,'COMPANY', 'Company Maintenance Menu', 'company.maintenance.menu',5),
       (24,'REGION', 'Region Menu', 'region.menu',5),
       (25,'DIVISION', 'Division Menu', 'division.menu',5),
       (26,'PO CONTROL', 'PO Control Menu', 'po.control.menu',5),
       (27,'VENDOR', 'Vendor Maintenance', 'vendor.maintenance',5),
       (28,'SHIP VIA', 'Ship Via Maintenance', 'ship.via.maintenance',5),
       (29,'VENDOR TERM CODE', 'Vendor Term Code', 'vendor.term.code',5),
       (30,'AP CONTROL', 'Account Payable Control', 'account.payable.control',5),
       (31,'CHART OF ACCOUNT', 'Chart of Account', 'chart.of.account',5),
       (32,'BANK', 'Bank Maintenance', 'bank.maintenance',5),
       (33,'GL CONTROL', 'General Ledger Control', 'general.ledger.control',5),
       (34,'FINANCIAL STATEMENT', 'Financial Statement', 'financial.statement',5)

;

CREATE TABLE module_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(50) CHECK ( char_length(trim(value)) > 1)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    program           VARCHAR(50) CHECK ( char_length(trim(program)) > 1)            NOT NULL,
    area_type_id      BIGINT REFERENCES area_type_domain (id)                        NOT NULL,
    UNIQUE (value, program)
);


INSERT INTO module_type_domain (id, value, description, localization_code, program,area_type_id)
VALUES (1, 'APADD', 'Add Invoices', 'add.invoices', 'APADD',1),
       (2, 'APAGERPT', 'Aging Report', 'aging.report', 'APAGERPT',1),
       (3, 'APCHECK', 'Print Checks', 'print.checks','APCHECK', 1),
       (4, 'APCHG', 'AP Change Invoices', 'change.invoices','APCHG',1),
       (5, 'APCHKRPT', 'Check Report', 'check.report','APCHKRPT',1),
       (6, 'APCLEAR', 'Clear Checks', 'clear.checks', 'APCLEAR',1),
       (7, 'APDEL', 'Delete Invoices', 'delete.invoices', 'APDEL', 1),
       (8, 'APGLRPT', 'G/L Analysis', 'gl.analysis', 'APGLRPT', 3),
       (9, 'APLST', 'Vendor Invoices', 'vendor.invoices', 'APRPT',1),
       (10, 'APPREVUE', 'Check Preview Rpt', 'check.preview.rpt', 'APPREVUE', 1),
       (11, 'APPURGE', 'Purge AP Records','purge.ap.records', 'APPURGE',1),
       (12, 'APRPT', 'AP Report','ap.report', 'APRPT',1),
       (13, 'APSEL', 'Select Invoices', 'select.invoices', 'APSEL',1),
       (14, 'APSHO','Show Invoices', 'show.invoices', 'APSHO',1),
       (15, 'APSTATUS', 'Vendor Status', 'vendor.status','APSTATUS',1),
       (16, 'APUTIL', 'AP Utilities', 'ap.utilities','APUTIL',1),
       (17, 'APVOID', 'Void Checks', 'void.checks', 'APVOID',1),
       (18, 'CASHOUT', 'Cash Requirements', 'cash.requirements', 'CASHOUT',1),
       (19, 'APPURGE', 'Purge Checks', 'purge.checks', 'APPURGE',1),
       (20, 'POADD','Add PO', 'add.po', 'POADD',4),
       (21, 'POCHG', 'Change PO', 'change.po', 'POCHG',4),
       (22, 'PODEL', 'Delete PO', 'delete.po', 'PODEL',4),
       (23, 'POINLOAD', 'Receive From PO', 'receive.from.po', 'POINLOAD',4),
       (24, 'POLST', 'List PO', 'list.po', 'POLST',4),
       (25, 'PARAMS', 'Control File Main', 'control.file.main', 'PARAMS',5),
       (26, 'POPURGE','Purge PO Records', 'purge.po.records', 'POPURGE',4),
       (27, 'POREC', 'Enter Receiving', 'enter.receiving','POREC',4),
       (28, 'PORECLST','List Receiving', 'list.receiving', 'PORECLST',4),
       (29, 'PORECRPT', 'Receiving Rpt', 'receiving.rpt', 'PORECRPT',4),
       (30, 'PORPT', 'PO Report', 'po.report', 'PORPT',4),
       (31, 'POSHO', 'Show PO', 'show.po', 'POSHO',4),
       (32, 'POUPDT', 'Update PO', 'update.po', 'POUPDT',4),
       (33, 'POUTIL', 'PO Utilities', 'po.utilities', 'POUTIL',4),
       (34, 'POWRKSHT','Recv Worksheets', 'recv.worksheets', 'POWRKSHT',4),
       (35, 'QUOTERPT','Quote Report', 'quote.report', 'QUOTERPT',4),
       (36, 'VDRQUOTE', 'Vendor Quotes', 'vendor.quotes', 'VDRQUOTE',4),
       (37, 'SPOADD', 'Special Orders', 'special.orders', 'SPOADD',4),
       (38, 'SPOLST', 'List Special Ord', 'list.special.ord', 'SPOLST',4),
       (39, 'SPOPRT', 'Prt Special Ord', 'prt.special.ord', 'SPOPRT',4),
       (40, 'POCAN', 'Cancel PO', 'cancel.po', 'POCAN',4),
       (41, 'POCOPY', 'Copy a PO', 'copy.a.po', 'POCOPY',4);

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
    id             BIGSERIAL                                 NOT NULL PRIMARY KEY,
    uu_row_id      UUID        DEFAULT uuid_generate_v1()    NOT NULL,
    time_created   TIMESTAMPTZ DEFAULT clock_timestamp()     NOT NULL,
    time_updated   TIMESTAMPTZ DEFAULT clock_timestamp()     NOT NULL,
    level          INTEGER                                   NOT NULL,
    module_type_id BIGINT REFERENCES module_type_domain (id) NOT NULL,
    company_id     BIGINT REFERENCES company (id)            NOT NULL,
    UNIQUE (company_id, module_type_id)
);
CREATE TRIGGER update_module_level_trg
    BEFORE UPDATE
    ON module
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
-- end user tables





