CREATE TABLE deposits_staging_deposit_type_domain
(
    id                                                   UUID         DEFAULT uuid_generate_v1()                        NOT NULL PRIMARY KEY,
    value                                                VARCHAR(10)                                                    NOT NULL,
    description                                          VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code                                    VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE
);
