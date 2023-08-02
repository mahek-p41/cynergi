
CREATE TABLE inventory
(
      id                                     UUID         DEFAULT uuid_generate_v1()                     NOT NULL PRIMARY KEY,
      time_created                           TIMESTAMPTZ  DEFAULT clock_timestamp()                      NOT NULL,
      time_updated                           TIMESTAMPTZ  DEFAULT clock_timestamp()                      NOT NULL,
      dataset                                TEXT                                                        NOT NULL,
      serial_number                          VARCHAR(50)                                                 NOT NULL,
      lookup_key                             VARCHAR(50)                                                 NOT NULL,
      lookup_key_type                        VARCHAR(50)                                                 NOT NULL,
      barcode                                VARCHAR(50)                                                 NOT NULL,
      alternate_id                           VARCHAR(50)                                                 NOT NULL,
      brand                                  VARCHAR(50)                                                 NOT NULL,
      model_number                           VARCHAR(50)                                                 NOT NULL,
      product_code                           VARCHAR(50)                                                 NOT NULL,
      description                            VARCHAR(50)                                                 NOT NULL,
      received_date                          DATE                                                        NOT NULL,
      original_cost                          NUMERIC(11,2)                                               NOT NULL,
      actual_cost                            NUMERIC(11,2)                                               NOT NULL,
      model_category                         VARCHAR(1)                                                  NOT NULL,
      times_rented                           INTEGER,
      total_revenue                          NUMERIC(11,2),
      remaining_value                        NUMERIC(11,2)                                               NOT NULL,
      sell_price                             NUMERIC(11,2)                                               NOT NULL,
      assigned_value                         NUMERIC(11,2),
      idle_days                              INTEGER,
      condition                              VARCHAR(15),
      invoice_number                         VARCHAR(20),
      inv_invoice_expensed_date              DATE,
      inv_purchase_order_number              VARCHAR(20),
      status_id                              BIGINT                                                      NOT NULL,
      model_id                               BIGINT,
      returned_date                          DATE,
      location                               INTEGER                                                     NOT NULL,
      status                                 VARCHAR(20)                                                 NOT NULL,
      store_id                               BIGINT                                                      NOT NULL,
      primary_location                       INTEGER                                                     NOT NULL,
      location_type                          INTEGER                                                     NOT NULL,
      received_location                      INTEGER                                                     NOT NULL,
      invoice_id                             UUID,
      inventory_changed_sw                   BOOLEAN        DEFAULT FALSE                                NOT NULL,
      changes_sent_to_current_state_sw       BOOLEAN        DEFAULT FALSE                                NOT NULL
);

COMMENT ON TABLE inventory IS 'Manage RTO inventory assets.';

CREATE TRIGGER update_inventory_trg
    BEFORE UPDATE
    ON inventory
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE INDEX inventory_received_location_idx ON inventory (received_location);
CREATE INDEX inventory_model_number_idx ON inventory (model_number);
CREATE INDEX inventory_invoice_number_idx ON inventory (invoice_number);
CREATE INDEX inventory_alternate_id_idx ON inventory (alternate_id);
CREATE INDEX inventory_serial_number_idx ON inventory (serial_number);
CREATE INDEX inventory_inv_purchase_order_number_idx ON inventory (inv_purchase_order_number);
CREATE INDEX inventory_received_date_idx ON inventory (received_date);
CREATE INDEX inventory_lookup_key_idx ON inventory (lookup_key);
CREATE INDEX inventory_dataset_idx ON inventory (dataset);
