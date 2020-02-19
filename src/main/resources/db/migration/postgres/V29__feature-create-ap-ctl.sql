CREATE TABLE accounts_payable_control (
    id                 BIGSERIAL                                            NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()               NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    company_id                          BIGINT  REFERENCES company(id)      NOT NULL,
    pay_after_discount_date             BOOLEAN DEFAULT FALSE               NOT NULL,
    reset_expense                       BOOLEAN DEFAULT FALSE               NOT NULL,
    use_rebates_indicator               BOOLEAN DEFAULT FALSE               NOT NULL,
    general_ledger_inventory_clearing_account  integer,
    trade_company_indicator                    BOOLEAN DEFAULT FALSE        NOT NULL,
    general_ledger_store_accounts_payable      integer,
    general_ledger_corporate_accounts_receivable    integer,
    general_ledger_corporate_sales             integer,
    general_ledger_corporate_purchase          integer,
    print_currency_indicator            varchar(1) DEFAULT 'N'              NOT NULL,
    lock_inventory_indicator            BOOLEAN DEFAULT FALSE               NOT NULL,
    purchase_order_number_required_indicator varchar(1)  DEFAULT 'V'        NOT NULL
);


CREATE UNIQUE INDEX co_code_index ON accounts_payable_control(company_id,code);

CREATE TRIGGER accounts_payable_control_trg
   BEFORE UPDATE
   ON accounts_payable_control
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
