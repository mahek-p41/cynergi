CREATE TABLE purchase_order_detail_element
(
    id                           UUID        DEFAULT uuid_generate_v1()                         NOT NULL PRIMARY KEY,
    time_created                 TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                          NOT NULL,
    time_updated                 TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                          NOT NULL,
    company_id                   UUID REFERENCES company (id)                                   NOT NULL,
    purchase_order_detail_id     UUID REFERENCES purchase_order_detail (id)                     NOT NULL,
    number                       BIGINT                                                         NOT NULL,
    vendor_model_number_id       VARCHAR(25)                                                    NOT NULL,
    element_desc                 VARCHAR(100) CHECK ( char_length(trim(element_desc)) > 1)      NOT NULL,
    po_qty_per_order             INTEGER                                                        NOT NULL,
    cost_per_po_qty              NUMERIC(11, 3)                                                 NOT NULL,
    color_id_sfk                 INTEGER,
    fabric_color_id_sfk          INTEGER
);
CREATE TRIGGER purchase_order_detail_element_trg
    BEFORE UPDATE
    ON purchase_order_detail_element
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
