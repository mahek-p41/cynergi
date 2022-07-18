DROP TRIGGER account_payable_distribution_template_trg ON account_payable_distribution_template;
ALTER TABLE account_payable_distribution_template DROP COLUMN name;
ALTER TABLE account_payable_distribution_template RENAME TO account_payable_distribution_template_detail;

CREATE TABLE account_payable_distribution_template
(
   id                         UUID        DEFAULT uuid_generate_v1()       NOT NULL PRIMARY KEY,
   time_created               TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()        NOT NULL,
   time_updated               TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()        NOT NULL,
   company_id                 UUID REFERENCES company (id)                 NOT NULL,
   name                       varchar(10)                                  NOT NULL,
   deleted                    BOOLEAN DEFAULT FALSE                        NOT NULL
);

CREATE TRIGGER account_payable_distribution_template_trg
   BEFORE UPDATE
   ON account_payable_distribution_template
   FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE TRIGGER account_payable_distribution_template_detail_trg
   BEFORE UPDATE
   ON account_payable_distribution_template_detail
   FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


ALTER TABLE account_payable_distribution_template_detail
ADD COLUMN template_id UUID REFERENCES account_payable_distribution_template (id);
