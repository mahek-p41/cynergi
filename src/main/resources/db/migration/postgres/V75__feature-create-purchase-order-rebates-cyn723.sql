CREATE TABLE purchase_order_rebate
(
    id                       UUID        DEFAULT uuid_generate_v1()     NOT NULL PRIMARY KEY,
    time_created             TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()      NOT NULL,
    time_updated             TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()      NOT NULL,
    purchase_order_header_id UUID REFERENCES purchase_order_header (id) NOT NULL,
    rebate_id                UUID REFERENCES rebate (id)                NOT NULL
);
CREATE TRIGGER purchase_order_rebate_trg
    BEFORE UPDATE
    ON purchase_order_rebate
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX purchase_order_header_idx
    ON purchase_order_rebate (purchase_order_header_id);
CREATE INDEX rebate_idx
    ON purchase_order_rebate (rebate_id);

COMMENT ON TABLE  purchase_order_rebate IS 'Table holds the rebates associated with a purchase order, one to many relationship.';
