CREATE TABLE financial_statement_type_domain
(
    id                INTEGER                                 NOT NULL PRIMARY KEY,
    value             VARCHAR(100)                            NOT NULL,
    description       VARCHAR(100)                            NOT NULL,
    localization_code VARCHAR(100)                            NOT NULL,
    UNIQUE(value)
);

COMMENT ON TABLE security_access_point_type_domain IS 'Financial Statement Types for HOA application.';

CREATE TABLE financial_statement_layout (
      id                                     UUID         DEFAULT uuid_generate_v1()                                 NOT NULL PRIMARY KEY,
      time_created                           TIMESTAMPTZ  DEFAULT clock_timestamp()                                  NOT NULL,
      time_updated                           TIMESTAMPTZ  DEFAULT clock_timestamp()                                  NOT NULL,
      company_id                             UUID         REFERENCES company (id)                                    NOT NULL,
      statement_type_id                      BIGINT       REFERENCES financial_statement_type_domain (id)       NOT NULL,
      name                                   VARCHAR(20)                                                             NOT NULL,
      header                                 VARCHAR(255)                                                            NOT NULL,
      deleted                                BOOLEAN      DEFAULT FALSE                                              NOT NULL
);

COMMENT ON TABLE financial_statement_layout IS 'Used to manage the financial statement layouts.';

CREATE TRIGGER financial_statement_layout_trg
    BEFORE UPDATE
    ON financial_statement_layout
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

ALTER TABLE financial_statement_layout
ADD CONSTRAINT financial_statement_layout_unique_constraint
UNIQUE (company_id, name, deleted);

CREATE TABLE financial_statement_section (
      id                                     UUID         DEFAULT uuid_generate_v1()                                 NOT NULL PRIMARY KEY,
      time_created                           TIMESTAMPTZ  DEFAULT clock_timestamp()                                  NOT NULL,
      time_updated                           TIMESTAMPTZ  DEFAULT clock_timestamp()                                  NOT NULL,
      company_id                             UUID         REFERENCES company (id)                                    NOT NULL,
      statement_layout_id                    UUID         REFERENCES financial_statement_layout (id)                 NOT NULL,
      name                                   VARCHAR(20)                                                             NOT NULL,
      total_name                             VARCHAR(255)                                                            NOT NULL,
      deleted                                BOOLEAN      DEFAULT FALSE                                              NOT NULL
);

CREATE TRIGGER financial_statement_section_trg
    BEFORE UPDATE
    ON financial_statement_section
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE TABLE financial_statement_group (
      id                                     UUID         DEFAULT uuid_generate_v1()                                 NOT NULL PRIMARY KEY,
      time_created                           TIMESTAMPTZ  DEFAULT clock_timestamp()                                  NOT NULL,
      time_updated                           TIMESTAMPTZ  DEFAULT clock_timestamp()                                  NOT NULL,
      company_id                             UUID         REFERENCES company (id)                                    NOT NULL,
      section_id                             UUID         REFERENCES financial_statement_section (id)                NOT NULL,
      name                                   VARCHAR(20)                                                             NOT NULL,
      total_name                             VARCHAR(255)                                                            NOT NULL,
      sort_order                             BIGINT                                                                  NOT NULL,
      contra_account                         BOOLEAN                                                                 NOT NULL,
      parenthesize                           VARCHAR(10)                                                                     ,
      underline_row_count                    BIGINT                                                                  NOT NULL,
      inactive                               BOOLEAN                                                                 NOT NULL,
      deleted                                BOOLEAN      DEFAULT FALSE                                              NOT NULL,
      parent_id                              UUID REFERENCES financial_statement_group (id)
);

CREATE TABLE group_to_account (
   company_id                             UUID         REFERENCES company (id)                                    NOT NULL,
   group_id                               UUID         REFERENCES financial_statement_group (id)                  NOT NULL,
   account_id                             UUID         REFERENCES account (id)                                    NOT NULL,
   CONSTRAINT uq_group_to_account UNIQUE
      ( company_id,
      account_id
      )
);

COMMENT ON TABLE financial_statement_layout IS 'Used to map GL accounts to groups.';
