CREATE TABLE general_ledger_control
(
    id                                              UUID        DEFAULT uuid_generate_v1() NOT NULL PRIMARY KEY,
    time_created                                    TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()  NOT NULL,
    time_updated                                    TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()  NOT NULL,
    company_id                                      UUID REFERENCES company (id)           NOT NULL,
    default_profit_center_sfk                       BIGINT                                 NOT NULL, --this is typically the home office location will be foreign key to store/home office
    default_account_payable_account_id              UUID REFERENCES account (id),
    default_account_payable_discount_account_id     UUID REFERENCES account (id),
    default_account_receivable_account_id           UUID REFERENCES account (id),
    default_account_receivable_discount_account_id  UUID REFERENCES account (id),
    default_account_misc_inventory_account_id       UUID REFERENCES account (id),
    default_account_serialized_inventory_account_id UUID REFERENCES account (id),
    default_account_unbilled_inventory_account_id   UUID REFERENCES account (id),
    default_account_freight_account_id              UUID REFERENCES account (id),
    UNIQUE (company_id)
);
CREATE TRIGGER general_ledger_control_trg
    BEFORE UPDATE
    ON general_ledger_control
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX general_ledger_company_id_idx
    ON general_ledger_control (company_id);
CREATE INDEX gl_default_ap_account_id_idx
    ON general_ledger_control (default_account_payable_account_id);
CREATE INDEX gl_default_ap_account_discount_id_idx
    ON general_ledger_control (default_account_payable_discount_account_id);
CREATE INDEX gl_default_ar_account_id_idx
    ON general_ledger_control (default_account_receivable_account_id);
CREATE INDEX gl_default_ar_discount_account_id_idx
    ON general_ledger_control (default_account_receivable_discount_account_id);
CREATE INDEX gl_default_misc_inventory_account_id_idx
    ON general_ledger_control (default_account_misc_inventory_account_id);
CREATE INDEX gl_default_serialized_inventory_account_id_idx
    ON general_ledger_control (default_account_serialized_inventory_account_id);
CREATE INDEX gl_default_unbilled_inventory_account_id_idx
    ON general_ledger_control (default_account_unbilled_inventory_account_id);
CREATE INDEX gl_default_account_freight_account_id_idx
    ON general_ledger_control (default_account_freight_account_id);
