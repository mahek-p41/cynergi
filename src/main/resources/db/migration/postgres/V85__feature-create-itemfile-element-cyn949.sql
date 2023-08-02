CREATE TABLE itemfile_element
(
   id                           UUID        DEFAULT uuid_generate_v1()                         NOT NULL PRIMARY KEY,
time_created                 TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                          NOT NULL,
time_updated                 TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                          NOT NULL,
company_id                   UUID REFERENCES company (id)                                   NOT NULL,
itemfile_id                  UUID                                                           NOT NULL,
itemfile_sfk                 INTEGER CHECK( number > 0 )                                    NOT NULL,
number                       INTEGER CHECK( number > 0 )                                    NOT NULL,
vendor_model_number          VARCHAR(25)                                                    NOT NULL,
element_desc                 VARCHAR(100) CHECK ( char_length(trim(element_desc)) > 1)      NOT NULL,
po_qty_per_order             INTEGER CHECK( number > 0 )                                    NOT NULL,
cost_per_po_qty              NUMERIC(11, 3)                                                 NOT NULL,
color_id_sfk                 INTEGER CHECK( number > 0 )                                    NOT NULL,
fabric_color_id_sfk          INTEGER CHECK( number > 0 )                                    NOT NULL
);
CREATE TRIGGER itemfile_element_trg
BEFORE UPDATE
ON itemfile_element
FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
