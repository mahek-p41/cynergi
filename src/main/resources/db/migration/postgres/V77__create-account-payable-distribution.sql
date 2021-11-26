CREATE TABLE account_payable_payment_distribution
(
   id                                  UUID        DEFAULT uuid_generate_v1()          NOT NULL PRIMARY KEY,
   time_created                        TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()           NOT NULL,
   time_updated                        TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()           NOT NULL,
   payment_id                          UUID REFERENCES account_payable_payment(id)     NOT NULL,
   distribution_account                UUID REFERENCES account(id)                     NOT NULL,
   distribution_profit_center_sfk      INTEGER                                         NOT NULL,
   distribution_amount                 NUMERIC(11,2)                                   NOT NULL
);

CREATE TRIGGER account_payable_payment_distribution_trg
   BEFORE UPDATE
   ON account_payable_payment_distribution
   FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
