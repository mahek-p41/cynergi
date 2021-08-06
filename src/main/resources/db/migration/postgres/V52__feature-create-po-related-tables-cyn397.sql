CREATE TABLE freight_term_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO freight_term_type_domain (id, value, description, localization_code)
VALUES (1, 'C', 'Collect', 'collect'),
       (2, 'P', 'Prepaid', 'prepaid');

CREATE TABLE exception_ind_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO exception_ind_type_domain (id, value, description, localization_code)
VALUES (1, 'E', 'Exception', 'exception'),
       (2, 'N', 'Normal', 'normal'),
       (3, 'P', 'Promo', 'promo'),
       (4, 'S', 'Special', 'special');

CREATE TABLE ship_location_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO ship_location_type_domain (id, value, description, localization_code)
VALUES (1, 'C', 'Customer', 'customer'),
       (2, 'S', 'Store', 'store');

CREATE TABLE purchase_order_requisition_indicator_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO purchase_order_requisition_indicator_type_domain(id, value, description, localization_code)
VALUES (1, 'P', 'Purchase Order', 'purchase.order'),
       (2, 'R', 'Requisition', 'requisition'),
       (3, 'D', 'Deleted', 'deleted');

CREATE TABLE purchase_order_header
(
    id                            UUID        DEFAULT uuid_generate_v1()                   NOT NULL PRIMARY KEY,
    time_created                  TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                    NOT NULL,
    time_updated                  TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                    NOT NULL,
    number                        BIGINT,
    company_id                    UUID REFERENCES company (id)                             NOT NULL,
    vendor_id                     UUID REFERENCES vendor (id)                              NOT NULL,
    status_type_id                BIGINT REFERENCES purchase_order_status_type_domain (id) NOT NULL,
    order_date                    DATE        DEFAULT CURRENT_DATE                         NOT NULL,
    type_id                       BIGINT REFERENCES purchase_order_type_domain (id)        NOT NULL,
    freight_on_board_type_id      BIGINT REFERENCES freight_on_board_type_domain (id)      NOT NULL,
    freight_term_type_id          BIGINT REFERENCES freight_term_type_domain (id)          NOT NULL,
    ship_location_type_id         BIGINT REFERENCES ship_location_type_domain (id)         NOT NULL,
    approved_by_id_sfk            INTEGER                                                  NOT NULL,
    total_amount                  NUMERIC(11, 2),
    received_amount               NUMERIC(11, 2),
    paid_amount                   NUMERIC(11, 2),
    purchase_agent_id_sfk         INTEGER                                                  NOT NULL,
    ship_via_id                   UUID REFERENCES ship_via (id)                            NOT NULL,
    required_date                 DATE                                                     NOT NULL,
    ship_to_id_sfk                INTEGER CHECK ( ship_to_id_sfk > 0 )                     NOT NULL,
    vendor_payment_term_id        UUID REFERENCES vendor_payment_term (id)                 NOT NULL,
    message                       TEXT,
    total_landed_amount           NUMERIC(11, 2),
    total_freight_amount          NUMERIC(11, 2),
    exception_ind_type_id         BIGINT REFERENCES exception_ind_type_domain (id)         NOT NULL,
    vendor_submitted_time         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP(), -- Z column POH-VENDOR-SUBMITTAL-DATE, POH-VENDOR-SUBMITTAL-TIME, POH-VENDOR-SUBMITTAL-HOUR, POH-VENDOR-SUBMITTAL-MINUTE, POH-VENDOR-SUBMITTAL-SECOND
    vendor_submitted_employee_sfk INTEGER,
    ecommerce_indicator           BOOLEAN     DEFAULT FALSE                                NOT NULL,
    customer_account_number_sfk   INTEGER,
    deleted                       BOOLEAN     DEFAULT FALSE                                NOT NULL
);
CREATE TRIGGER update_purchase_order_header_trg
    BEFORE UPDATE
    ON purchase_order_header
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
CREATE INDEX purchase_order_header_company_id_idx ON purchase_order_header (company_id);
CREATE INDEX purchase_order_header_vendor_id_idx ON purchase_order_header (vendor_id);
CREATE INDEX purchase_order_header_status_type_id_idx ON purchase_order_header (status_type_id);
CREATE INDEX purchase_order_header_type_id_idx ON purchase_order_header (type_id);
CREATE INDEX purchase_order_header_freight_on_board_type_id_idx ON purchase_order_header (freight_on_board_type_id);
CREATE INDEX purchase_order_header_freight_term_type_id_idx ON purchase_order_header (freight_term_type_id);
CREATE INDEX purchase_order_header_ship_location_type_id_idx ON purchase_order_header (ship_location_type_id);
CREATE INDEX purchase_order_header_ship_via_id_idx ON purchase_order_header (ship_via_id);
CREATE INDEX purchase_order_header_payment_term_type_id_idx ON purchase_order_header (vendor_payment_term_id);
CREATE INDEX purchase_order_header_exception_ind_type_id_idx ON purchase_order_header (exception_ind_type_id);
CREATE INDEX purchase_order_header_vend_submitted_emp_idx ON purchase_order_header (vendor_submitted_employee_sfk);
CREATE INDEX purchase_order_header_customer_acct_nbr_idx ON purchase_order_header (customer_account_number_sfk);

-- Begin purchase_order_detail
CREATE TABLE purchase_order_detail
(
    id                                           UUID        DEFAULT uuid_generate_v1()                                   NOT NULL PRIMARY KEY,
    time_created                                 TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                                    NOT NULL,
    time_updated                                 TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                                    NOT NULL,
    number                                       BIGINT CHECK ( number > 0 ),
    purchase_order_header_id                     UUID REFERENCES purchase_order_header (id)                               NOT NULL,
    company_id                                   UUID REFERENCES company (id)                                             NOT NULL,
    itemfile_number_sfk                          CHARACTER VARYING(25) CHECK (CHAR_LENGTH(TRIM(itemfile_number_sfk)) > 1) NOT NULL,
    order_quantity                               INTEGER                                                                  NOT NULL,
    received_quantity                            INTEGER                                                                  NOT NULL,
    cost                                         NUMERIC(11, 3)                                                           NOT NULL,
    message                                      TEXT,
    color_id_sfk                                 INTEGER, -- This is a softkey and will hold the value associated with furncol view.
    fabric_id_sfk                                INTEGER, -- This is a softkey and will hold the value associated with furnfab view.
    cancelled_quantity                           INTEGER,
    cancelled_temp_quantity                      INTEGER,
    ship_to_id_sfk                               INTEGER CHECK ( ship_to_id_sfk > 0 )                                     NOT NULL,
    required_date                                DATE,
    date_ordered                                 DATE,
    freight_per_item                             NUMERIC(11, 2),
    temp_quantity_to_receive                     INTEGER,
    vendor_id                                    UUID REFERENCES vendor (id)                                              NOT NULL,
    last_received_date                           DATE,
    landed_cost                                  NUMERIC(11, 3),
    status_type_id                               BIGINT REFERENCES purchase_order_status_type_domain (id)                 NOT NULL,
    purchase_order_requisition_indicator_type_id BIGINT REFERENCES purchase_order_requisition_indicator_type_domain (id)  NOT NULL,
    exception_ind_type_id                        BIGINT REFERENCES exception_ind_type_domain (id)                         NOT NULL,
    converted_purchase_order_number              INTEGER     DEFAULT 0                                                    NOT NULL,
    approved_ind                                 BOOLEAN     DEFAULT FALSE                                                NOT NULL,
    deleted                                      BOOLEAN     DEFAULT FALSE                                                NOT NULL
);
CREATE TRIGGER update_purchase_order_detail_trg
    BEFORE UPDATE
    ON purchase_order_detail
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
CREATE INDEX purchase_order_detail_purchase_order_header_id_idx ON purchase_order_detail (purchase_order_header_id);
CREATE INDEX purchase_order_detail_number_id_idx ON purchase_order_detail (purchase_order_header_id);
CREATE INDEX purchase_order_detail_company_id_idx ON purchase_order_detail (company_id);
CREATE INDEX purchase_order_detail_vendor_id_idx ON purchase_order_detail (vendor_id);
CREATE INDEX purchase_order_detail_status_type_id_idx ON purchase_order_detail (status_type_id);
CREATE INDEX purchase_order_detail_po_requisition_ind_type_id_idx ON purchase_order_detail (purchase_order_requisition_indicator_type_id);
CREATE INDEX purchase_order_detail_exception_ind_type_id_idx ON purchase_order_detail (exception_ind_type_id);
-- End purchase_order_detail
