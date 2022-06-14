CREATE TABLE agreement_signing_status_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(15) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO agreement_signing_status_type_domain (id, value, description, localization_code)
VALUES (1, 'NEW', 'New', 'new'),
       (2, 'IN_PROGRESS', 'In_Progress', 'in_progress'),
       (3, 'FULLY_SIGNED', 'Fully_Signed', 'fully_signed'),
       (4, 'CANCELLED', 'Cancelled', 'cancelled');

CREATE TABLE agreement_signing (
    id                        UUID        DEFAULT uuid_generate_v1()          NOT NULL PRIMARY KEY,
    time_created              TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()           NOT NULL,
    time_updated              TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()           NOT NULL,
    company_id                UUID        REFERENCES company (id)             NOT NULL,
    store_number_sfk          BIGINT                                          NOT NULL,
    primary_customer_number   BIGINT                                          NOT NULL,
    secondary_customer_number BIGINT                                          NOT NULL,
    agreement_number          BIGINT                                          NOT NULL,
    agreement_type            VARCHAR(1)                                      NOT NULL,
    status_id                 INTEGER REFERENCES agreement_signing_status_type_domain (id)  NOT NULL,
    external_signature_id     VARCHAR(200)                               NOT NULL
);
CREATE TRIGGER update_agreement_signing_trg
BEFORE UPDATE
   ON agreement_signing
FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
CREATE INDEX agreement_signing_store_number_sfk_idx
   ON agreement_signing (store_number_sfk);
CREATE INDEX agreement_signing_customer_idx
   ON agreement_signing (primary_customer_number);
CREATE INDEX agreement_signing_agreement_idx
   ON agreement_signing (agreement_number);
ALTER TABLE agreement_signing
   ADD CONSTRAINT company_customer_agreement_uq UNIQUE (company_id, primary_customer_number, secondary_customer_number, agreement_number);

CREATE TABLE aws_token (
    id                        UUID        DEFAULT uuid_generate_v1()          NOT NULL PRIMARY KEY,
    time_created              TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()           NOT NULL,
    time_updated              TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()           NOT NULL,
    company_id                UUID        REFERENCES company (id)             NOT NULL,
    store_number_sfk          BIGINT                                          NOT NULL,
    token                     VARCHAR(60)                                     NOT NULL
);
CREATE TRIGGER update_aws_token_trg
BEFORE UPDATE
   ON aws_token
FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
CREATE INDEX aws_token_store_number_sfk_idx
   ON aws_token (store_number_sfk);
ALTER TABLE aws_token
   ADD CONSTRAINT company_store_token_uq UNIQUE (company_id, store_number_sfk, token);
