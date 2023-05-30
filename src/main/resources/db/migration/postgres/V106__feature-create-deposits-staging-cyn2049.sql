CREATE TABLE deposits_staging
(
    id                                                   UUID        DEFAULT uuid_generate_v1()                      NOT NULL PRIMARY KEY,
    time_created                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                       NOT NULL,
    time_updated                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                       NOT NULL,
    company_id                                           UUID REFERENCES company (id)                                NOT NULL,
    verify_id                                            UUID REFERENCES verify_staging (id)                         NOT NULL,
    store                                                INTEGER                                                     NOT NULL,
    date                                                 DATE                                                        NOT NULL,
    deposit_type_id                                      UUID REFERENCES deposits_staging_deposit_type_domain (id)   NOT NULL,
    deposit_amount                                       NUMERIC(13,2)                                               NOT NULL
);

CREATE TRIGGER update_deposits_staging_trg
    BEFORE UPDATE
    ON deposits_staging
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE INDEX deposits_staging_company_id_idx
    ON deposits_staging (company_id);

CREATE INDEX deposits_staging_verify_id_idx
    ON deposits_staging (verify_id);

CREATE INDEX deposits_staging_deposit_type_id_idx
    ON deposits_staging (deposit_type_id);
