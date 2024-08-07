CREATE TABLE purchase_order_header_status_action
(
    id                       UUID        DEFAULT uuid_generate_v1()                   NOT NULL PRIMARY KEY,
    time_created             TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                    NOT NULL,
    time_updated             TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                    NOT NULL,
    changed_by_sfk           INTEGER CHECK ( changed_by_sfk > 0 )                     NOT NULL, --employee that was responsible for status change
    status_id                BIGINT REFERENCES purchase_order_status_type_domain (id) NOT NULL,
    purchase_order_header_id UUID REFERENCES purchase_order_header (id)               NOT NULL,
    UNIQUE (status_id, purchase_order_header_id)
);
CREATE TRIGGER update_purchase_order_header_status_action_trg
   BEFORE UPDATE
   ON purchase_order_header_status_action
   FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
CREATE INDEX purchase_order_header_status_action_idx ON purchase_order_header_status_action (purchase_order_header_id);
