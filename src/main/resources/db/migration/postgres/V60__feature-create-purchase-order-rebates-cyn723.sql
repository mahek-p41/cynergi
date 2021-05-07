CREATE TABLE purchase_order_rebate
(
    id                                               BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                                        UUID        DEFAULT uuid_generate_v1()                                      NOT NULL,
    time_created                                     TIMESTAMPTZ DEFAULT clock_timestamp()                                       NOT NULL,
    time_updated                                     TIMESTAMPTZ DEFAULT clock_timestamp()                                       NOT NULL,
    purchase_order_header_id                         BIGINT REFERENCES purchase_order_header (id)                                NOT NULL,
    rebate_id                                        BIGINT REFERENCES rebate (id)                                               NOT NULL
 );
CREATE TRIGGER purchase_order_rebate_trg
    BEFORE UPDATE
    ON purchase_order_rebate
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE INDEX purchase_order_header_idx ON purchase_order_rebate (purchase_order_header_id);
CREATE INDEX rebate_idx ON purchase_order_rebate (rebate_id);


COMMENT ON TABLE  purchase_order_rebate IS 'Table holds the rebates associated with a purchase order, one to many relationship.';
