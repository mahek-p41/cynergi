CREATE TABLE verify_staging
(
    id                                                   UUID        DEFAULT uuid_generate_v1()                  NOT NULL PRIMARY KEY,
    time_created                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    time_updated                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    company_id                                           UUID REFERENCES company (id)                            NOT NULL,
    verify_id                                            UUID REFERENCES verify (id)                             NOT NULL,
    store                                                INTEGER                                                 NOT NULL,
    date                                                 DATE                                                    NOT NULL,
    deposit_type_id                                      UUID REFERENCES deposit (id)                            NOT NULL,
    deposit_amount                                       NUMERIC(13,2)                                           NOT NULL
);

CREATE TRIGGER update_verify_staging_trg
    BEFORE UPDATE
    ON verify_staging
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE INDEX verify_staging_company_id_idx
    ON verify_staging (company_id);
