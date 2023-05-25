CREATE TABLE inventory_end_of_month
(
    id                                                   UUID        DEFAULT uuid_generate_v1()                  NOT NULL PRIMARY KEY,
    time_created                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    time_updated                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    company_id                                           UUID REFERENCES company (id)                            NOT NULL,
    store_number_sfk                                     BIGINT                                                  NOT NULL,
    year                                                 INTEGER CHECK (char_length(trim(year ::text)) = 4)      NOT NULL,
    month                                                INTEGER                                                 NOT NULL,
    serial_number                                        VARCHAR(30)                                             NOT NULL,
    cost                                                 NUMERIC(13, 2)                                          NOT NULL,
    net_book_value                                       NUMERIC(13, 2)                                          NOT NULL,
    book_depreciation                                    NUMERIC(13, 2)                                          NOT NULL,
    asset_account_id                                     UUID REFERENCES account (id)                            NOT NULL,
    contra_asset_account_id                              UUID REFERENCES account (id)                            NOT NULL,
    model                                                VARCHAR(18)                                             NOT NULL,
    alternate_id                                         VARCHAR(30)                                             NOT NULL,
    current_inv_indr                                     BOOLEAN     DEFAULT FALSE                               NOT NULL,
    macrs_previous_fiscal_year_end_cost                  NUMERIC(13, 2),
    macrs_previous_fiscal_year_end_depr                  NUMERIC(13, 2),
    macrs_previous_fiscal_year_end_amt_depr              NUMERIC(13, 2),
    macrs_previous_fiscal_year_end_date                  DATE,
    macrs_latest_fiscal_year_end_cost                    NUMERIC(13, 2),
    macrs_latest_fiscal_year_end_depr                    NUMERIC(13, 2),
    macrs_latest_fiscal_year_end_amt_depr                NUMERIC(13, 2),
    macrs_previous_fiscal_year_bonus                     NUMERIC(8, 7),
    macrs_latest_fiscal_year_bonus                       NUMERIC(8, 7)
);

CREATE TRIGGER update_inventory_end_of_month_trg
    BEFORE UPDATE
    ON inventory_end_of_month
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE INDEX inventory_end_of_month_company_id_idx
    ON inventory_end_of_month (company_id);

CREATE INDEX inventory_end_of_month_asset_account_id_idx
    ON inventory_end_of_month (asset_account_id);

CREATE INDEX inventory_end_of_month_contra_asset_account_id_idx
    ON inventory_end_of_month (contra_asset_account_id);
