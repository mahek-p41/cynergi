
CREATE TABLE account_payable_invoice_schedule
(
      id                                     UUID         DEFAULT uuid_generate_v1()                                 NOT NULL PRIMARY KEY,
      time_created                           TIMESTAMPTZ  DEFAULT clock_timestamp()                                  NOT NULL,
      time_updated                           TIMESTAMPTZ  DEFAULT clock_timestamp()                                  NOT NULL,
      company_id                             UUID         REFERENCES company (id)                                    NOT NULL,
      invoice_id                             UUID         REFERENCES account_payable_invoice (id)                    NOT NULL,
      schedule_date                          DATE                                                                    NOT NULL,
      payment_sequence_number                INTEGER                                                                 NOT NULL,
      amount_to_pay                          NUMERIC(13,2)                                                           NOT NULL,
      bank_id                                UUID         REFERENCES bank (id),
      external_payment_type_id               BIGINT       REFERENCES account_payable_payment_type_type_domain(id)    NOT NULL,
      external_payment_number                VARCHAR(20),
      external_payment_date                  DATE,
      selected_for_processing                BOOLEAN      DEFAULT FALSE                                              NOT NULL,
      payment_processed                      BOOLEAN      DEFAULT FALSE                                              NOT NULL,
      deleted                                BOOLEAN      DEFAULT FALSE                                              NOT NULL
);

COMMENT ON TABLE account_payable_invoice_schedule IS 'Used to manage the payment schedules for Account Payable invoices.';

CREATE TRIGGER update_account_payable_invoice_schedule_trg
    BEFORE UPDATE
    ON account_payable_invoice_schedule
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE INDEX account_payable_invoice_schedule_company_id_idx ON account_payable_invoice_schedule (company_id);
CREATE INDEX account_payable_invoice_schedule_invoice_id_idx ON account_payable_invoice_schedule (invoice_id);
CREATE INDEX account_payable_invoice_schedule_bank_id_idx ON account_payable_invoice_schedule (bank_id);
CREATE INDEX account_payable_invoice_schedule_external_payment_type_id_idx ON account_payable_invoice_schedule (external_payment_type_id);
