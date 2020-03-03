CREATE TABLE area_type_domain (
	 id integer                                                                  NOT NULL PRIMARY KEY,
    name varchar(50) CHECK ( char_length(trim(name)) > 1)                       NOT NULL,
	 code varchar(10) CHECK ( char_length(trim(code)) > 1)                       NOT NULL,
	 description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
	 localization_code varchar(100) CHECK( char_length(trim(localization_code)) > 1) NOT NULL,
	 UNIQUE(name, code)
 );

COMMENT ON TABLE area_type_domain IS 'Domain table contains the individual areas/systems available for a company to implement. i.e. PO, AP, GL';

INSERT INTO area_type_domain (id,name,code,description,localization_code)
Values
(1,'ACCOUNT PAYABLE', 'AP', 'Account Payable Area and Functionality', 'account.payable.area.and.functionality') ,
(2,'BANK RECONCILIATION', 'BR', 'Bank Reconciliation Area and Functionality', 'bank.reconciliation.area.and.functionality') ,
(3,'GENERAL LEDGER', 'GL', 'General Ledger Area and Functionality', 'general.ledger.area.and.functionality') ,
(4,'PURCHASE ORDER', 'PO', 'Purchase Order and Requistion Area and Functionality', 'purchase.order.and.requistion.area.and.functionality');


CREATE TABLE company_to_area (
   company_id BIGINT REFERENCES company(id)                                    NOT NULL,
   area_type_id  BIGINT REFERENCES area_type_domain(id)                        NOT NULL,
   UNIQUE(company_id,area_type_id)
);

COMMENT ON TABLE company_to_area IS 'Join table which joins the company to areas that the company has agreed to implement';


CREATE TABLE menu_type_domain(
    id     integer                                                              NOT NULL PRIMARY KEY,
    area_type_id BIGINT REFERENCES area_type_domain (id)                        NOT NULL,
    name varchar (50) CHECK ( char_length(trim(name)) > 1)                      NOT NULL,
    description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
    localization_code varchar(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (area_type_id,name)
);

INSERT INTO menu_type_domain (id,area_type_id,name,description,localization_code)
Values
(1,1,'AP MENU', 'Account Payable Menu', 'account.payable.menu') ,
(2,2,'BKRECON MENU', 'Bank Reconciliation Menu', 'bank.reconciliation.menu') ,
(3,3,'GL MENU',  'General Ledger Menu', 'general.ledger.menu') ,
(4,4,'PO MENU', 'Purchase Order Menu', 'purchase.order.menu'),
(5,1,'INVOICE MENU', 'Account Payable Invoice Menu', 'account.payable.invoice.menu') ,
(6,1,'CHECK MENU', 'Account Payable Check Menu', 'account.payable.check.menu') ,
(7,1,'INQUIRY MENU',  'Account Payable Inguiry Menu', 'account.payable.inquiry.menu') ,
(8,1,'REPORTS MENU', 'Accounts Payable Reports Menu', 'account.payable.reports.menu'),
(9,1,'MONTH END MENU',  'Account Payable Month End Menu', 'account.payable.month.end.menu') ,
(10,1,'CONTROL FILE RECORDS', 'Accounts Payable Control File Maintenance', 'account.payable.control.file.maintenance'),
(11,2,'VERIFY/UPDATE STORE DEPOSIT', 'View and Validate Store Deposit', 'view.and.validate.store.deposit') ,
(12,2,'SEND STORE DEPOSIT TO BANK REC', 'Complete Store Deposit Process With Bank ', 'complete.store.deposit.process.with.bank') ,
(13,2,'CLEAR OUTSTANDING ITEMS',  'Outstanding Items Maintenance', 'outstanding.items.maintenance') ,
(14,2,'LIST BANK TRANSACTIONS', 'View Bank Related Transactions', 'view.bank.related.transactions'),
(15,2,'RECONCILE BANK ACCOUNT',  'Reconcile Bank Account', 'reconcile.bank.account') ,
(16,2,'PRINT BANK TRANSACTIONS', 'Allows Printing Of Transactions', 'allows.printing.of.transactions'),
(17,2,'UTILITIES', 'Maintenance Files', 'maintenance.files'),
(18,4,'PROCESSING',  'Purchase Order Management', 'purchase.order.management') ,
(19,4,'INQUIRY', 'View Purchase Order Information', 'view.purchase.order.information'),
(20,4,'REPORTS',  'Run Reports Related To Purchase Orders', 'run.reports.related.to.purchase.orders') ,
(21,4,'UTILITIES', 'Allows Changes To Existing Purchase Orders', 'allows.changes.to.existing.purchase.orders'),
(22,4,'CONTROL FILE', 'Maintenance For Purchase Order Related Information', 'maintenance.for.purchase.order.related.information'),
(23,3,'JOURNAL ENTRY PROCESSING',  'Journal Entry Management', 'journal.entry.management') ,
(24,3,'ACCOUNT INQUIRY', 'View Journal Entry Information', 'view.journal.entry.information'),
(25,3,'MONTH END PROCESSING',  'Month End Process Management', 'month.end.process.management') ,
(26,3,'UTILITIES', 'Service Journal Entries', 'service.journal.entries'),
(27,3,'CONTROL FILE', 'Maintenance For Journal Accounts', 'maintenance.for.journal.accounts');

CREATE TABLE module_type_domain(
    id     integer                                                             NOT NULL PRIMARY KEY,
    area_type_id BIGINT REFERENCES area_type_domain(id)                        NOT NULL,
    name varchar (50) CHECK ( char_length(trim(name)) > 1)                     NOT NULL,
    program varchar(50) CHECK ( char_length(trim(description)) > 1)            NOT NULL,
    description varchar (100) CHECK ( char_length(trim(description)) > 1)      NOT NULL,
    localization_code varchar(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (name,program)
);

INSERT INTO module_type_domain (id,area_type_id,name,program,description,localization_code)
Values
(1,2,'APADD','AP','Add Invoices','add.invoices'),
(2,2,'APAGERPT','APAGERPT','Aging Report','aging.report'),
(3,2,'APCHECK','APCHECK','Print Checks','print.checks'),
(4,2,'APCHG','AP','AP Change Invoices','change.invoices'),
(5,2,'APCHKLST','APCHKRPT','List 'AP Checks','list.'ap.checks'),
(6,2,'APCHKRPT','APCHKRPT','Check Report','check.report'),
(7,2,'APCLEAR','APCLEAR','Clear Checks','clear.checks'),
(8,2,'APDEL','AP','Delete Invoices','delete.invoices'),
(9,2,'APGLRPT','APGLRPT','G/L Analysis','g/l.analysis'),
(10,2,'APLST','APRPT','Vendor Invoices','vendor.invoices'),
(11,2,'APPREVUE','APPREVUE','Check Preview Rpt','check.preview.rpt'),
(12,2,'APPURGE','APPURGE','Purge 'AP Records','purge.'ap.records'),
(13,2,'APRPT','APRPT',''AP Report',''ap.report'),
(14,2,'APSEL','APSEL','Select Invoices','select.invoices'),
(15,2,'APSHO','APX','Show Invoices','show.invoices'),
(16,2,'APSTATUS','APSTATUS','Vendor Status','vendor.status'),
(17,2,'APUTIL','APUTIL',''AP Utilities',''ap.utilities'),
(18,2,'APVOID','APVOID','Void Checks','void.checks'),
(19,2,'CASHOUT','CASHOUT','Cash Requirements','cash.requirements'),
(20,2,'CHKPURGE','APPURGE','Purge Checks','purge.checks'),
(21,4,'ITMQUOTE','ITMQUOTE','Item Quotes','item.quotes'),
(22,4,'POADD','PO','Add PO','add.po'),
(23,4,'POCHG','PO','Change PO','change.po'),
(24,4,'PODEL','PO','Delete PO','delete.po'),
(25,4,'POINLOAD','POINLOAD','Receive From PO','receive.from.po'),
(26,4,'POLST','POLISTS','List PO','list.po'),
(27,4,'POPARAMS','PARAMS','Control File Main','control.file.main'),
(28,4,'POPURGE','POPURGE','Purge PO Records','purge.po.records'),
(29,4,'POREC','POREC','Enter Receiving','enter.receiving'),
(30,4,'PORECLST','RECRPT','List Receiving','list.receiving'),
(31,4,'PORECRPT','RECRPT','Receiving Rpt','receiving.rpt'),
(32,4,'PORPT','PORPT','PO Report','po.report'),
(33,4,'POSHO','POX','Show PO','show.po'),
(34,4,'POUPDT','POUPDT','Update PO','update.po'),
(35,4,'POUTIL','POUTIL','PO Utilities','po.utilities'),
(36,4,'POWRKSHT','POX','Recv Worksheets','recv.worksheets'),
(37,4,'QUOTERPT','QUOTERPT','Quote Report','quote.report'),
(38,4,'VDRQUOTE','VDRQUOTE','Vendor Quotes','vendor.quotes'),
(39,4,'SPOADD','SPO','Special Orders','special.orders'),
(40,4,'SPOLST','SPO','List Special Ord','list.special.ord'),
(41,4,'SPOPRT','SPO','Prt Special Ord','prt.special.ord'),
(42,4,'POCAN','PO','Cancel PO','cancel.po'),
(43,4,'POCOPY','POCOPY','Copy a PO','copy.a.po');

CREATE TABLE menu_to_module (
   menu_type_id BIGINT REFERENCES menu_type_domain(id)                              NOT NULL,
   module_type_id  BIGINT REFERENCES module_type_domain(id)                         NOT NULL,
   UNIQUE(menu_type_id,module_type_id)
);

COMMENT ON TABLE menu_to_module IS 'Join table which joins the menus to the modules';

INSERT INTO menu_to_module (menu_type_id, module_type_id)
Values
(5,1),
(5,4),
(5,8),
(5,10),
(5,15),
(6,5),
(6,6),
(6,7),
(6,3),
(18,22),
(18,23),
(18,24),
(18,25),
(18,26),
(19,33),
(19,34),
(20,31),
(20,32);



CREATE TABLE module_level (
   id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
   uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
   time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
   time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
   module_type_id BIGINT REFERENCES module_type_domain (id)                    NOT NULL,
   module_level integer                                                        NOT NULL,
   UNIQUE(module_type_id,module_level)
);


CREATE TRIGGER update_module_level_trg
   BEFORE UPDATE
   ON module_level
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
