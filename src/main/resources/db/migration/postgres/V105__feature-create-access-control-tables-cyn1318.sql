CREATE TABLE security_access_point_type_domain
(
    id                INTEGER                                 NOT NULL PRIMARY KEY,
    value             VARCHAR(100)                            NOT NULL,
    description       VARCHAR(100)                            NOT NULL,
    localization_code VARCHAR(100)                            NOT NULL,
    area_id bigint REFERENCES area_type_domain(id)
);

COMMENT ON COLUMN security_access_point_type_domain.value
    IS 'Most Cases This represents the Z program name.';

COMMENT ON TABLE security_access_point_type_domain IS 'Security points for HOA application.';

CREATE INDEX security_access_point_type_domain_area_id_idx ON security_access_point_type_domain (area_id);


INSERT INTO security_access_point_type_domain (id, value, description, localization_code, area_id)
VALUES
       (1, 'APADD', 'Add Account Payable Invoices', 'add.account.payable.invoices', (SELECT id FROM area_type_domain WHERE value = 'AP')),
       (2, 'APCHG', 'Change Account Payable Invoices', 'change.account.payable.invoices',(SELECT id FROM area_type_domain WHERE value = 'AP')),
       (3, 'APDEL', 'Delete Account Payable Invoices', 'delete.account.payable.invoices',(SELECT id FROM area_type_domain WHERE value = 'AP')),
       (4, 'APCHECK', 'Print Account Payable Checks', 'print.account.payable.checks',(SELECT id FROM area_type_domain WHERE value = 'AP')),
       (5, 'APCLEAR', 'Clear Account Payable Payments', 'clear.account.payable.payments', (SELECT id FROM area_type_domain WHERE value = 'AP')),
       (6, 'APSEL', 'Select Account Payable Invoices', 'select.account.payable.invoices', (SELECT id FROM area_type_domain WHERE value = 'AP')),
       (7, 'APPREVUE', 'Account Payable Check Preview', 'print.account.payable.check.preview', (SELECT id FROM area_type_domain WHERE value = 'AP')),
       (8, 'APVOID', 'Void Account Payable Payment', 'void.account.payable.payment', (SELECT id FROM area_type_domain WHERE value = 'AP')),
       (9, 'APSTATUS', 'Vendor Statistics', 'vendor.statistics',(SELECT id FROM area_type_domain WHERE value = 'AP')),
       (10, 'POADD','Add Purchase Order', 'add.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (11, 'POCHG', 'Change Purchase Order', 'change.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (12, 'PODEL', 'Delete Purchase Order', 'delete.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (13, 'POPURGE','Purge Purchase Order Records', 'purge.purchase.order.records', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (14, 'POINLOAD', 'Receive From Purchase Order', 'receive.from.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (15, 'POUPDT', 'Update Purchase Order', 'update.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (16, 'SPOADD', 'Add Special Order', 'add.special.order', (SELECT id FROM area_type_domain WHERE value = 'SPO')),
       (17, 'POCAN', 'Cancel Purchase Order', 'cancel.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (18, 'POCOPY', 'Copy a Purchase Order', 'copy.a.purchase.order', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (19, 'INVCRED', 'Return Item for Credit to Vendor', 'return.item.for.credit.to.vendor', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (20, 'INVAVAIL', 'inventory.availability', 'inventory.availability', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (21, 'POSTAT', 'Change Purchase Order Status', 'change.purchase.order.status', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (22, 'VENDOR', 'Vendor', 'vendor.maintenance', null),
       (23, 'PODETCHG', 'Adjust Purchase Order Receiving Quantities', 'adjust.purchase.order.receiving.quantities', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (24, 'POWRKSHT','Purchase Order Worksheet', 'purchase.order.worksheet', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (25, 'ADDCOMP', 'Add a Company Record', 'add.a.company.record', null),
       (26, 'CHGCOMP', 'Change a Company Record', 'change.a.company.record', null),
       (27, 'DELCOMP', 'Delete a Company Record', 'delete.a.company.record',null ),
       (28, 'POPARAMS', 'Purchase Order Control', 'purchase.order.control',null),
       (29, 'ADDVEND', 'Add a New Vendor', 'add.a.new.vendor',null),
       (30, 'CHGVEND', 'Change a Vendor', 'change.a.vendor',null),
       (31, 'DELVEND', 'Delete a Vendor', 'delete.a.vendor',null),
       (32, 'ADDVIA', 'Add a Ship Via Code', 'add.a.ship.via.code',null),
       (33, 'CHGVIA', 'Change a Ship Via Code', 'change.a.ship.via.code',null),
       (34, 'DELVIA', 'Delete a Ship Via Code', 'delete.a.ship.via.code',null),
       (35, 'APPURGE', 'Purge Account Payable Records','purge.account.payable.records', (SELECT id FROM area_type_domain WHERE value = 'AP')),
       (36, 'APPARAMS', 'Account Payable Control', 'account.payable.control',null),
       (37, 'ADDACCT', 'Add a General Ledger Account', 'add.a.general.ledger.account', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (38, 'CHGACCT', 'Change a General Ledger Account', 'chg.a.general.ledger.account', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (39, 'DELACCT', 'Delete a General Ledger Account', 'delete.a.general.ledger.account',(SELECT id FROM area_type_domain WHERE value = 'GL')),
       (40, 'CPYACCT', 'Copy General Ledger Account', 'copy.general.ledger.account',(SELECT id FROM area_type_domain WHERE value = 'GL')),
       (41, 'ADDBANK', 'Add a Bank', 'add.a.bank',null),
       (42, 'CHGBANK', 'Change a Bank', 'change.a.bank',null),
       (43, 'DELBANK', 'Delete a Bank', 'delete.a.bank',null),
       (44, 'GLPARAMS', 'General Ledger Master File', 'general.ledger.master.file',null),
       (45, 'ADDLAY', 'Add a Statement Layout', 'add.a.statement.layout', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (46, 'CHGLAY', 'Change a Statement Layout', 'change.a.statement.layout', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (47, 'DELLAY', 'Delete a Statement Layout', 'delete.a.statement.layout',(SELECT id FROM area_type_domain WHERE value = 'GL')),
       (48, 'FORMLAY', 'Format a Sample Statement', 'format.a.sample.statement', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (49, 'CPYLAY', 'Copy a Statement Layout', 'Copy.a.statement.layout', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (50, 'ADDGLCOD', 'Add a New General Ledger Code', 'add.a.new.general.ledger.code', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (51, 'CHGGLCOD', 'Change a G/L Code', 'change.a.gl.code', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (52, 'DELGLCOD', 'Delete a G/L Code', 'delete.a.gl.code', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (53, 'ADDAPDST', 'Add a Distribution Template', 'add.a.distribution.template', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (54, 'CHGAPDST', 'Change a Distribution Template', 'change.a.distribution.template', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (55, 'DELAPDST', 'Delete a Distribution Template', 'delete.a.distribution.template', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (56, 'SHOAPDST', 'Show a Distribution Template', 'show.a.distribution.template', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (57, 'PRTAPDST', 'Print a Distribution Template', 'print.a.distribution.template', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (58,'GLJE','Create General Ledger Journal Entries','create.general.ledger.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (59,'GLPFTBAL','General Ledger Profit Center Trial Balance Report','general.ledger.profit.center.trial.balance.report', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (60,'GLRECUR','General Ledger Recurring Maintenance','general.ledger.recurring.maintenance', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (61,'GLYREND','General Ledger Year End','general.ledger.year.end', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (62,'GLFINSTATE','General Ledger Financials','general.ledger.financials', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (63,'GLDATES','Maintain Open Account Payable & General Ledger Periods','maintain.open.account.payable.&.general.ledger.periods', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (64,'GLSTATUS','General Ledger Account Inquiry','general.ledger.account.inquiry', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (65,'GLSUM','Summary to General Ledger Interface','summary.to.general.ledger.interface', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (66,'GLRECLASS','Summary to General Ledger Reclass','summary.to.general.ledger.reclass', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (67,'GLLSTJE','General Ledger List Pending Journal Entries','general.ledger.list.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (68,'GLPOSTJE','General Ledger Post Pending Journal Entries','general.ledger.post.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (69,'GLADDJE','General Ledger Add Pending Journal Entries','general.ledger.add.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (70,'GLCHGJE','General Ledger Change Pending Journal Entries','general.ledger.change.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (71,'GLDELJE','General Ledger Delete Pending Journal Entries','general.ledger.delete.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (72,'GLEXPORTJE','General Ledger Export Pending Journal Entries','general.ledger.export.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (73,'GLPURGEJE','General Ledger Purge Pending Journal Entries','general.ledger.purge.pending.journal.entries', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (74,'BKRECCLR','Clear/Unclear Bank Reconciliation Items','clear.unclear.bank.reconciliation.items', (SELECT id FROM area_type_domain WHERE value = 'BR')),
       (75,'BKRECLST','List Bank Reconciliation Transactions','list.bank.reconciliation.transactions', (SELECT id FROM area_type_domain WHERE value = 'BR')),
       (76,'BKRECACT','Reconcile Bank Account','reconcile.bank.account', (SELECT id FROM area_type_domain WHERE value = 'BR')),
       (77,'BKRECRPT','Bank Reconciliation Report','bank.reconciliation.report', (SELECT id FROM area_type_domain WHERE value = 'BR')),
       (78,'BKRECCHG','Modify Bank Transactions','modify.bank.transactions', (SELECT id FROM area_type_domain WHERE value = 'BR')),
       (79,'POREPORT','Purchase Order Reports','purchase.order.reports', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (80,'APREPORT','Account Payable Reports','account.payable.reports', (SELECT id FROM area_type_domain WHERE value = 'AP')),
       (81,'BRREPORT','Bank Reconciliation Reports','bank.reconciliation.reports', (SELECT id FROM area_type_domain WHERE value = 'BR')),
       (82,'GLREPORT','General Ledger Reports','general.ledger.reports', (SELECT id FROM area_type_domain WHERE value = 'GL')),
       (83,'APPAYTO','Change Account Payable Pay To','change.account.payable.pay.to', (SELECT id FROM area_type_domain WHERE value = 'AP')),
       (84,'POCHGCST','Purchase Order Change Cost','purchase.order.change.cost', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (85,'POSHIPTO','Purchase Order Change Ship To','purchase.order.change.ship.to', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (86,'POOVERRIDE','Purchase Order Override','purchase.order.override', (SELECT id FROM area_type_domain WHERE value = 'PO')),
       (87,'POVIEWMFGCST','Purchase Order View Manufacturer Cost','purchase.order.view.manufacturer.cost', (SELECT id FROM area_type_domain WHERE value = 'PO'));

-- This table is replacing the department table in Cynergi

CREATE TABLE security_group (
    id              UUID         DEFAULT uuid_generate_v1()                 NOT NULL PRIMARY KEY,
    time_created    TIMESTAMPTZ  DEFAULT clock_timestamp()                  NOT NULL,
    time_updated    TIMESTAMPTZ  DEFAULT clock_timestamp()                  NOT NULL,
    key             VARCHAR(100) CHECK ( char_length(key) > 1 )             NOT NULL,
    description     VARCHAR(100) CHECK ( char_length(description) > 1 )     NOT NULL,
    company_id      UUID       REFERENCES company (id)                    NOT NULL
);

COMMENT ON TABLE security_group IS 'Security groups to organize access control.';

CREATE TRIGGER update_security_group_trg
    BEFORE UPDATE
    ON security_group
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

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
      security_group_id   UUID   REFERENCES security_group (id)            NOT NULL
);

COMMENT ON TABLE security_group_to_security_access_point IS 'Mapping of employees to security groups.';

CREATE INDEX employee_to_security_group_id_idx ON security_group(id);
