CREATE TABLE security_access_point_type_domain
(
    id                INTEGER                                 NOT NULL PRIMARY KEY,
    value             VARCHAR(100)                            NOT NULL,
    description       VARCHAR(100)                            NOT NULL,
    localization_code VARCHAR(100)                            NOT NULL,
    area_id bigint    REFERENCES area_type_domain(id),
    UNIQUE(value)
);

COMMENT ON COLUMN security_access_point_type_domain.value
    IS 'Most Cases This represents the Z program name.';

COMMENT ON TABLE security_access_point_type_domain IS 'Security points for HOA application.';

CREATE INDEX security_access_point_type_domain_area_id_idx ON security_access_point_type_domain (area_id);


INSERT INTO security_access_point_type_domain (id, value, description, localization_code, area_id)
VALUES
   (1,'AP','Accounts Payable Module','accounts.payable.module', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (2,'AP1099','Accounts Payable Print 1099s','accounts.payable.print.1099s', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (3,'APADD','Add Account Payable Invoices','add.account.payable.invoices', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (4,'APAGERPT','Accounts Payable Aging Report','accounts.payable.aging.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (5,'APCASHOUT','Accounts Payable Cash Requirements Report','accounts.payable.cash.requirements.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (6,'APCHECK','Print Account Payable Checks','print.account.payable.checks',(SELECT id FROM area_type_domain WHERE value = 'AP')),
   (7,'APCHG','Change Account Payable Invoices','change.account.payable.invoices',(SELECT id FROM area_type_domain WHERE value = 'AP')),
   (8,'APCHKLST','Accounts Payable List Payments','accounts.payable.list.payments', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (9,'APCHKRPT','Accounts Payable Payment Report','accounts.payable.payment.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (10,'APCLEAR','Clear Account Payable Payments','clear.account.payable.payments', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (11,'APDEL','Delete Account Payable Invoices','delete.account.payable.invoices',(SELECT id FROM area_type_domain WHERE value = 'AP')),
   (12,'APEXPENS','Accounts Payable Expense Report','accounts.payable.expense.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (13,'APFLOWANAL','Accounts Payable Cash Flow Report','accounts.payable.cash.flow.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (14,'APGLREPORT','Accounts Payable to General Ledger Interface','accounts.payable.to.general.ledger.interface', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (15,'APINVMNT','Accounts Payable Inventory Inquiry','accounts.payable.inventory.inquiry', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (16,'APINVOICEMNT','Accounts Payable Invoice Maintenance','accounts.payable.maintenance',(SELECT id FROM area_type_domain WHERE value = 'AP')),
   (17,'APLST','Accounts Payable List Vendor Invoices','accounts.payable.list.vendor.invoices', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (18,'APMONTHEND','Accounts Payable Month End','accounts.payable.month.end',(SELECT id FROM area_type_domain WHERE value = 'AP')),
   (19,'APPAYTO','Change Account Payable Pay To','change.account.payable.pay.to', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (20,'APPMTMNT','Accounts Payable Payment Maintenance','accounts.payable.payment.maintenance',(SELECT id FROM area_type_domain WHERE value = 'AP')),
   (21,'APPREVUE','Account Payable Check Preview Report','account.payable.check.preview.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (22,'APPURGE','Accounts Payable Purge ','accounts.payable.purge', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (23,'APRECUR','Accounts Payable Recurring Invoices','accounts.payable.recurring.invoices', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (24,'APRECURADD','Accounts Payable Recurring Invoices Add','accounts.payable.recurring.invoices.add', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (25,'APRECURCHG','Accounts Payable Recurring Invoices Change','accounts.payable.recurring.invoices.change', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (26,'APRECURDEL','Accounts Payable Recurring Invoices Delete','accounts.payable.recurring.invoices.delete', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (27,'APRECURLST','Accounts Payable Recurring Invoices List','accounts.payable.recurring.invoices.list', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (28,'APRECURPRT','Accounts Payable Recurring Invoices Report','accounts.payable.recurring.invoices.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (29,'APRECURSHO','Accounts Payable Recurring Invoices Show','accounts.payable.recurring.invoices.show', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (30,'APRECURTRN','Accounts Payable Recurring Invoices Transfer','accounts.payable.recurring.invoices.transfer', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (31,'APREPORT','Accounts Payable Reports','accounts.payable.reports', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (32,'APRPT','Accounts Payable Invoice Report','accounts.payable.invoice.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (33,'APSEL','Accounts Payable Select Invoices By Vendor ','accounts.payable.select.invoices.by.vendor', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (34,'APSELDUE','Accounts Payable Select By Due Date','accounts.payable.select.by.due.date', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (35,'APSHO','Accounts Payable Invoice Inquiry','accounts.payable.invoice.inquiry', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (36,'APSTATUS','Vendor Statistics','vendor.statistics',(SELECT id FROM area_type_domain WHERE value = 'AP')),
   (37,'APTRLBAL','Accounts Payable Vendor Balance Report','accounts.payable.vendor.balance.report', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (38,'APVOID','Void Account Payable Payment','void.account.payable.payment', (SELECT id FROM area_type_domain WHERE value = 'AP')),
   (39,'BK','Bank Reconciliation Module','bank.reconciliation.module', (SELECT id FROM area_type_domain WHERE value = 'BR')),
   (40,'BKRECACT','Reconcile Bank Account','reconcile.bank.account', (SELECT id FROM area_type_domain WHERE value = 'BR')),
   (41,'BKRECCHG','Modify Bank Transactions','modify.bank.transactions', (SELECT id FROM area_type_domain WHERE value = 'BR')),
   (42,'BKRECCLR','Clear/Unclear Bank Reconciliation Items','clear.unclear.bank.reconciliation.items', (SELECT id FROM area_type_domain WHERE value = 'BR')),
   (43,'BKRECLST','List Bank Reconciliation Transactions','list.bank.reconciliation.transactions', (SELECT id FROM area_type_domain WHERE value = 'BR')),
   (44,'BKRECRPT','Bank Reconciliation Report','bank.reconciliation.report', (SELECT id FROM area_type_domain WHERE value = 'BR')),
   (45,'GL','General Ledger Module','general.ledger.module', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (46,'GLACCTBAL','General Ledger Recalculate Account Balances','general.ledger.recalculate.account.balances', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (47,'GLACCTUSERPT','General Ledger Account Usage on Financial Stmts Report','general.ledger.account.usage.on.financial.stmts.report', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (48,'GLADDJE','General Ledger Add Pending Journal Entries','general.ledger.add.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (49,'GLANAL','General Ledger Profit Center Analysis','general.ledger.profit.center.analysis', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (50,'GLCHGJE','General Ledger Change Pending Journal Entries','general.ledger.change.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (51,'GLDATES','Maintain Open Account Payable & General Ledger Periods','maintain.open.account.payable.&.general.ledger.periods', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (52,'GLDELJE','General Ledger Delete Pending Journal Entries','general.ledger.delete.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (53,'GLEXPORTJE','General Ledger Export Pending Journal Entries','general.ledger.export.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (54,'GLFINSTATE','General Ledger Financials','general.ledger.financials', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (55,'GLJE','Create General Ledger Journal Entries','create.general.ledger.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (56,'GLJEMNT','General Ledger Journal Entry Maintenance','general.ledger.journal.entry.maintenance', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (57,'GLLSTJE','General Ledger List Pending Journal Entries','general.ledger.list.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (58,'GLMONTHEND','General Ledger Month End','general.ledger.month.end', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (59,'GLPFTBAL','General Ledger Profit Center Trial Balance Report','general.ledger.profit.center.trial.balance.report', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (60,'GLPOSTJE','General Ledger Post Pending Journal Entries','general.ledger.post.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (61,'GLPURGEDET','General Ledger Purge GL Detail Records','general.ledger.purge.gl.detail.records', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (62,'GLPURGEJE','General Ledger Purge Pending Journal Entries','general.ledger.purge.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (63,'GLRCNRPT','General Ledger Reconciliation Report','general.ledger.reconciliation.report', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (64,'GLRECLASS','Summary to General Ledger Reclass','summary.to.general.ledger.reclass', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (65,'GLRECUR','General Ledger Recurring Maintenance','general.ledger.recurring.maintenance', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (66,'GLRECURADD','General Ledger Add Recurring Journal Entry','general.ledger.add.recurring.journal.entry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (67,'GLRECURCHG','General Ledger Change Recurring Journal Entry','general.ledger.change.recurring.journal.entry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (68,'GLRECURDEL','General Ledger Delete Recurring Journal Entry','general.ledger.delete.recurring.journal.entry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (69,'GLRECURLST','General Ledger List Recurring Journal Entry','general.ledger.list.recurring.journal.entry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (70,'GLRECURPRT','General Ledger Report Recurring Journal Entry','general.ledger.report.recurring.journal.entry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (71,'GLRECURSHO','General Ledger Show Recurring Journal Entry','general.ledger.show.recurring.journal.entry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (72,'GLRECURTRN','General Ledger Transfer Recurring Journal Entry','general.ledger.transfer.recurring.journal.entry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (73,'GLREPORT','General Ledger Reports','general.ledger.reports', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (74,'GLREVERSAL','General Ledger Reversal ','general.ledger.reversal', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (75,'GLREVERSALCHG','General Ledger Reversal Change','general.ledger.reversal.change', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (76,'GLREVERSALDEL','General Ledger Reversal Delete','general.ledger.reversal.delete', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (77,'GLREVERSALPOST','General Ledger Reversal Post','general.ledger.reversal.post', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (78,'GLRPTJE','General Ledger Report Pending Journal Entries','general.ledger.report.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (79,'GLRPTTRAN','General Ledger Transactions by Source Code Report','general.ledger.transactions.by.source.code.report', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (80,'GLSRCH','General Ledger Inquiry/Search Report','general.ledger.inquiry.search.report', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (81,'GLSTAGINGMOVE','General Ledger Move Staging','general.ledger.move.staging', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (82,'GLSTAGINGSHOW','General Ledger Show Staging ','general.ledger.show.staging', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (83,'GLSTATUS','General Ledger Account Inquiry','general.ledger.account.inquiry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (84,'GLSUM','Summary to General Ledger Interface','summary.to.general.ledger.interface', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (85,'GLTRIALBAL','General Ledger Trial Balance Report','general.ledger.trial.balance.report', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (86,'GLUTIL','General Ledger Utilities','general.ledger.utilities', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (87,'GLWRKSHT','General Ledger Trial Balance Worksheet Report','general.ledger.worksheet.report', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (88,'GLYREND','General Ledger Year End','general.ledger.year.end', (SELECT id FROM area_type_domain WHERE value = 'GL')),
   (89,'MCF','Master Control Files Module','master.control.files.module',null),
   (90,'MCFAPPARAMS','Account Payable Control','account.payable.control',null),
   (91,'MCFAPPARAMSCHG','Account Payable Control Change','account.payable.control.change',null),
   (92,'MCFAREACHG','Change an Area Record','change.an.area.record',null),
   (93,'MCFBANKADD','Add a Bank','add.a.bank',null),
   (94,'MCFBANKCHG','Change a Bank','change.a.bank',null),
   (95,'MCFBANKDEL','Delete a Bank','delete.a.bank',null),
   (96,'MCFBANKRPT','Bank Report','bank.report',null),
   (97,'MCFCOAADD','Add a General Ledger Account','add.a.general.ledger.account',null),
   (98,'MCFCOACHG','Change a General Ledger Account','chg.a.general.ledger.account',null),
   (99,'MCFCOADEL','Delete a General Ledger Account','delete.a.general.ledger.account',null),
   (100,'MCFCOARPT','General Ledger Account Report','general.ledger.account.report',null),
   (101,'MCFCOMPADD','Add a Company Record','add.a.company.record',null),
   (102,'MCFCOMPCHG','Change a Company Record','change.a.company.record',null),
   (103,'MCFDISTTEMPADD','Add a Distribution Template','add.a.distribution.template',null),
   (104,'MCFDISTTEMPCHG','Change a Distribution Template','change.a.distribution.template',null),
   (105,'MCFDISTTEMPDEL','Delete a Distribution Template','delete.a.distribution.template',null),
   (106,'MCFDISTTEMPPRT','Print a Distribution Template','print.a.distribution.template',null),
   (107,'MCFDISTTEMPSHO','Show a Distribution Template','show.a.distribution.template',null),
   (108,'MCFDIVADD','Add a Division','add.a.division',null),
   (109,'MCFDIVCHG','Change a Division','change.a.division',null),
   (110,'MCFGLCODADD','Add a General Ledger Source Code','add.a.general.ledger.source.code',null),
   (111,'MCFGLCODCHG','Change a General Ledger Source Code','change.a.general.ledger.source.code',null),
   (112,'MCFGLCODDEL','Delete a General Ledger Source Code','add.a.general.ledger.source.code',null),
   (113,'MCFGLCODRPT','General Ledger Source Code Report','general.ledger.source.code.report',null),
   (114,'MCFGLPARAMS','General Ledger Control','general.ledger.control',null),
   (115,'MCFGLPARAMSCHG','General Ledger Control Change','general.ledger.control.change',null),
   (116,'MCFHOMEOFFICE','Master Control Files Home Office','master.control.files.home.office',null),
   (117,'MCFLAYADD','Add a Financial Statement Layout','add.a.financial.statement.layout',null),
   (118,'MCFLAYCHG','Change a Financial Statement Layout','change.a.financial.statement.layout',null),
   (119,'MCFLAYCOPY','Copy a Financial Statement Layout','copy.a.financial.statement.layout',null),
   (120,'MCFLAYDEL','Delete a Financial Statement Layout','delete.a.financial.statement.layout',null),
   (121,'MCFLAYFORM','Format a Financial Sample Statement','format.a.sample.financial.statement',null),
   (122,'MCFLAYRPT','Financial Statement Layout Report','financial.statement.layout.report',null),
   (123,'MCFPOPARAMS','Purchase Order Control','purchase.order.control',null),
   (124,'MCFPOPARAMSCHG','Purchase Order Control Change','purchase.order.control.change',null),
   (125,'MCFREGADD','Add a Region','add.a.region',null),
   (126,'MCFREGCHG','Change a Region','change.a.region',null),
   (127,'MCFRESTRICTED','Master Control Files Restricted','master.control.files.restricted',null),
   (128,'MCFSTOREREGCHG','Change Region for Store','change.region.for.store',null),
   (129,'MCFVENDGROUPADD','Add a Vendor Group','add.a.vendor.group',null),
   (130,'MCFVENDGROUPCHG','Change a Vendor Group','change.a.vendor.group',null),
   (131,'MCFVENDGROUPDEL','Delete a Vendor Group','delete.a.vendor.group',null),
   (132,'MCFVENDGROUPRPT','Vendor Group Report','vendor.group.report',null),
   (133,'MCFVENDORADD','Add a New Vendor','add.a.new.vendor',null),
   (134,'MCFVENDORCHG','Change a Vendor','change.a.vendor',null),
   (135,'MCFVENDORDEL','Delete a Vendor','delete.a.vendor',null),
   (136,'MCFVENDORRPT','Vendor Report','vendor.report',null),
   (137,'MCFVENDREBATEADD','Add a Vendor Rebate','add.a.vendor.rebate',null),
   (138,'MCFVENDREBATECHG','Change a Vendor Rebate','change.a.vendor.rebate',null),
   (139,'MCFVENDREBATEDEL','Delete a Vendor Rebate','delete.a.vendor.rebate',null),
   (140,'MCFVENDREBATERPT','Vendor Rebate Report','vendor.rebate.report',null),
   (141,'MCFVENDTERMADD','Add a Vendor Term Code','add.a.vendor.term.code',null),
   (142,'MCFVENDTERMCHG','Change a Vendor Term Code','change.a.vendor.term.code',null),
   (143,'MCFVENDTERMDEL','Delete a Vendor Term Code','delete.a.vendor.term.code',null),
   (144,'MCFVENDTERMRPT','Vendor Term Code Report','vendor.term.code.report',null),
   (145,'MCFVIAADD','Add a Ship Via Code','add.a.ship.via.code',null),
   (146,'MCFVIACHG','Change a Ship Via Code','change.a.ship.via.code',null),
   (147,'MCFVIADEL','Delete a Ship Via Code','delete.a.ship.via.code',null),
   (148,'MCFVIARPT','Ship Via Code Report','ship.via.code.report',null),
   (149,'PO','Purchase Order Module','purchase.order.module', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (150,'POADD','Add Purchase Order','add.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (151,'POCAN','Cancel Purchase Order','cancel.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (152,'POCHG','Change Purchase Order','change.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (153,'POCHGCST','Purchase Order Change Cost','purchase.order.change.cost', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (154,'POCOPY','Copy a Purchase Order','copy.a.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (155,'PODEL','Delete Purchase Order','delete.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (156,'PODETCHG','Adjust Purchase Order Receiving Quantities','adjust.purchase.order.receiving.quantities', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (157,'PODLST','Purchase Order List Details','purchase.order.list.details', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (158,'POLST','Purchase Order List','purchase.order.list', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (159,'POMNT','Purchase Order Maintenance','purchase.order.maintenance', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (160,'POOVERRIDE','Purchase Order Override','purchase.order.override', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (161,'POPURGE','Purge Purchase Order Records','purge.purchase.order.records', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (162,'POREPORT','Purchase Order Reports','purchase.order.reports', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (163,'PORPT','Purchase Order Report with Export','purchase.order.report.with.export', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (164,'POSADD','Add Purchase Order Special Order','add.purchase.order.special.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (165,'POSHIPTO','Purchase Order Change Ship To','purchase.order.change.ship.to', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (166,'POSHO','Purchase Order Inquiry','purchase.order.inquiry', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (167,'POSTAT','Change Purchase Order Status','change.purchase.order.status', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (168,'POUPDT','Update Purchase Order','update.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (169,'POVIEWMFGCST','Purchase Order View Manufacturer Cost','purchase.order.view.manufacturer.cost', (SELECT id FROM area_type_domain WHERE value = 'PO')),
   (170,'POWRKSHT','Purchase Order Worksheet','purchase.order.worksheet', (SELECT id FROM area_type_domain WHERE value = 'PO'));



-- This table is replacing the department table in Cynergi

CREATE TABLE security_group (
    id              UUID         DEFAULT uuid_generate_v1()                 NOT NULL PRIMARY KEY,
    time_created    TIMESTAMPTZ  DEFAULT clock_timestamp()                  NOT NULL,
    time_updated    TIMESTAMPTZ  DEFAULT clock_timestamp()                  NOT NULL,
    value           VARCHAR(100) CHECK ( char_length(value) > 1 )           NOT NULL,
    description     VARCHAR(100) CHECK ( char_length(description) > 1 )     NOT NULL,
    company_id      UUID         REFERENCES company (id)                    NOT NULL,
    deleted         BOOLEAN      DEFAULT FALSE                              NOT NULL,
    UNIQUE(value, company_id)
);

COMMENT ON TABLE security_group IS 'Security groups to organize access control.';

CREATE TRIGGER update_security_group_trg
    BEFORE UPDATE
    ON security_group
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX security_group_company_id_idx ON security_group (company_id);

-- This table is a join table sgtsap

CREATE TABLE security_group_to_security_access_point (
      security_group_id          UUID     REFERENCES security_group (id)                       NOT NULL,
      security_access_point_id   BIGINT   REFERENCES security_access_point_type_domain (id)    NOT NULL
);

COMMENT ON TABLE security_group_to_security_access_point IS 'Mapping of each security point to a security group.';

CREATE INDEX sgtsap_security_group_id_idx ON security_group_to_security_access_point(security_group_id);
CREATE INDEX sgtsap_security_access_point_id_idx ON security_group_to_security_access_point(security_access_point_id);

-- This table is a join table using the employee_id_sfk which uses the fastinfo materalized view, system_employees_fimvw

CREATE TABLE employee_to_security_group (
      employee_id_sfk     INTEGER                                          NOT NULL,
      security_group_id   UUID      REFERENCES security_group (id)         NOT NULL,
      deleted             BOOLEAN   DEFAULT FALSE                          NOT NULL
);

COMMENT ON TABLE employee_to_security_group IS 'Mapping of employees to security groups.';

CREATE INDEX employee_to_security_group_id_idx ON security_group(id);
