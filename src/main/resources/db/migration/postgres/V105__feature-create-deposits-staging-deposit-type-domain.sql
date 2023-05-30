CREATE TABLE deposits_staging_deposit_type_domain
(
    id                                                   UUID        DEFAULT uuid_generate_v1()                      NOT NULL PRIMARY KEY,
    value                                                VARCHAR(10)                                                 NOT NULL,
    description                                          VARCHAR(100)                                                NOT NULL,
    localization_code                                    VARCHAR(100)                                                NOT NULL
);

CREATE TRIGGER update_deposits_staging_deposit_type_domain_trg
    BEFORE UPDATE
    ON deposits_staging_deposit_type_domain
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
