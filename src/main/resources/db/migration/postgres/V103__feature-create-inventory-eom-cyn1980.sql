CREATE TABLE inventory_end_of_month
(
    id                                  UUID        DEFAULT uuid_generate_v1()                  NOT NULL PRIMARY KEY,
    time_created                        TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    time_updated                        TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    company_id                          UUID REFERENCES company (id)                            NOT NULL,
    store_number_sfk                    BIGINT                                                  NOT NULL,
    year                                INTEGER CHECK (char_length(trim(year)) = 4),
    month                               INTEGER,
    inv_serial_number                   VARCHAR(10),
    inv_cost                            NUMERIC(13, 2),
    net_book_value                      NUMERIC(13, 2),
    book_depreciation                   NUMERIC(13, 2),
    asset_account_id                    UUID REFERENCES asset_account (id)                      NOT NULL, --need to check reference table
    contra_asset_account_id             UUID REFERENCES contra_asset_account (id)               NOT NULL, --need to check reference table
    inv_model                           VARCHAR(18),
    inv_alt_id                          VARCHAR(30),
    current_inv_indr                    BOOLEAN     DEFAULT FALSE                               NOT NULL,
    macrs_pfy_end_cost                  NUMERIC(13, 2),
    macrs_pfy_end_depr                  NUMERIC(13, 2),
    macrs_pfy_end_amt_depr              NUMERIC(13, 2),
    macrs_pfy_end_date                  INTEGER,
    macrs_lfy_end_cost                  NUMERIC(13, 2),
    macrs_lfy_end_depr                  NUMERIC(13, 2),
    macrs_lfy_end_amt_depr              NUMERIC(13, 2),
    macrs_pfy_bonus                     NUMERIC(8, 7),
    macrs_lfy_bonus                     NUMERIC(8, 7)
);

CREATE TRIGGER update_inventory_end_of_month_trg
    BEFORE UPDATE
    ON vendor
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE OR REPLACE FUNCTION inventory_end_of_month_search_update_fn()
    RETURNS TRIGGER AS
$$
DECLARE
    invNum CONSTANT TEXT := CAST(new.number AS TEXT);
    invName CONSTANT TEXT := new.name;
BEGIN
    new.search_vector :=
        setweight(to_tsvector(invNum), 'A') ||
        setweight(to_tsvector(invName), 'B');

    RETURN new;
END;
$$
    LANGUAGE plpgsql STRICT;

CREATE TRIGGER inventory_end_of_month_search_update_trg
    BEFORE INSERT OR UPDATE
    ON vendor FOR EACH ROW EXECUTE PROCEDURE inventory_end_of_month_search_update_fn();

CREATE company_id_idx
    ON inventory_end_of_month (company_id);

CREATE asset_account_id_idx
    ON inventory_end_of_month (asset_account_id);

CREATE contra_asset_account_id_idx
    ON inventory_end_of_month (contra_asset_account_id);
