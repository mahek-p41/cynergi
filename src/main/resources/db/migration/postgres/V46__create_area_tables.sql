CREATE TABLE area_type_domain (
    id                 INTEGER                                                        NOT NULL PRIMARY KEY,
    value              VARCHAR(50)  CHECK ( char_length(trim(value)) > 1)             NOT NULL,
    description        VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code  VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

CREATE TABLE area (
    id           UUID        DEFAULT uuid_generate_v1()          NOT NULL PRIMARY KEY,
    time_created TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()           NOT NULL,
    time_updated TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()           NOT NULL,
    area_type_id INTEGER     REFERENCES area_type_domain (id)    NOT NULL,
    company_id   UUID        REFERENCES company (id)             NOT NULL,
    UNIQUE (company_id, area_type_id)
);
CREATE TRIGGER update_area_trg
BEFORE UPDATE
   ON area
FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


INSERT INTO area_type_domain (id, value, description, localization_code)
VALUES (1, 'AP', 'Account Payable', 'account.payable'),
       (2, 'BR', 'Bank Reconciliation', 'bank.reconciliation'),
       (3, 'GL', 'General Ledger', 'general.ledger'),
       (4, 'PO', 'Purchase Order', 'purchase.order'),
       (5, 'DARWILL', 'Darwill Upload', 'darwill.upload'),
       (6, 'SIGNATURE_CAPTURE', 'Online Signature Capture', 'signature.capture')
;
