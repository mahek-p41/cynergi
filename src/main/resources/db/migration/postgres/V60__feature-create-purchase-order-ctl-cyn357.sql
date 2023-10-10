CREATE TABLE purchase_order_status_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO purchase_order_status_type_domain (id, value, description, localization_code)
VALUES (1, 'B', 'Backorder', 'backorder'),
       (2, 'C', 'Cancelled', 'cancelled'),
       (3, 'H', 'Hold', 'hold'),
       (4, 'O', 'Open', 'open'),
       (5, 'P', 'Paid', 'paid'),
       (6, 'R', 'Received', 'received');


CREATE TABLE purchase_order_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO purchase_order_type_domain (id, value, description, localization_code)
VALUES (1, 'P', 'Purchase Order', 'purchase.order'),
       (2, 'R', 'Requisition', 'requisition'),
       (3, 'D', 'Deletes', 'deletes');


CREATE TABLE update_purchase_order_cost_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO update_purchase_order_cost_type_domain (id, value, description, localization_code)
VALUES (1, 'B', 'Both Purchase Order and Requisition', 'both.purchase.order.and.requisition'),
       (2, 'N', 'No Update', 'no.update'),
       (3, 'P', 'Purchase Order Only', 'purchase.order.only'),
       (4, 'R', 'Requisition Only', 'requisition.only');

CREATE TABLE approval_required_flag_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO approval_required_flag_type_domain (id, value, description, localization_code)
VALUES (1, 'B', 'Both Purchase Order and Requisition', 'both.purchase.order.and.requisition'),
       (2, 'N', 'No Approval', 'no.approval'),
       (3, 'P', 'Purchase Order Only', 'purchase.order.only'),
       (4, 'R', 'Requisition Only', 'requisition.only');

CREATE TABLE default_account_payable_status_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO default_account_payable_status_type_domain (id, value, description, localization_code)
VALUES (1, 'H', 'Hold', 'hold'),
       (2, 'O', 'Open', 'open');

CREATE TABLE default_purchase_order_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO default_purchase_order_type_domain (id, value, description, localization_code)
VALUES (1, 'P', 'Purchase Order', 'purchase.order'),
       (2, 'R', 'Requisition', 'requisition');


CREATE TABLE purchase_order_control
(
    id                                     UUID        DEFAULT uuid_generate_v1()                             NOT NULL PRIMARY KEY,
    time_created                           TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                              NOT NULL,
    time_updated                           TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                              NOT NULL,
    company_id                             UUID REFERENCES company (id)                                       NOT NULL,
    drop_five_characters_on_model_number   BOOLEAN     DEFAULT FALSE                                          NOT NULL,
    update_account_payable                 BOOLEAN     DEFAULT FALSE                                          NOT NULL,
    print_second_description               BOOLEAN     DEFAULT FALSE                                          NOT NULL,
    default_account_payable_status_type_id INTEGER REFERENCES default_account_payable_status_type_domain (id) NOT NULL,
    print_vendor_comments                  BOOLEAN     DEFAULT FALSE                                          NOT NULL,
    include_freight_in_cost                BOOLEAN     DEFAULT FALSE                                          NOT NULL,
    update_cost_on_model                   BOOLEAN     DEFAULT FALSE                                          NOT NULL,
    default_vendor_id                      UUID REFERENCES vendor (id),
    update_purchase_order_cost_type_id     BIGINT REFERENCES update_purchase_order_cost_type_domain (id)      NOT NULL,
    default_purchase_order_type_id         BIGINT REFERENCES default_purchase_order_type_domain (id)          NOT NULL,
    sort_by_ship_to_on_print               BOOLEAN     DEFAULT TRUE                                           NOT NULL,
    invoice_by_location                    BOOLEAN     DEFAULT FALSE                                          NOT NULL,
    validate_inventory                     BOOLEAN     DEFAULT FALSE                                          NOT NULL,
    default_approver_id_sfk                INTEGER,
    approval_required_flag_type_id         BIGINT REFERENCES approval_required_flag_type_domain (id)          NOT NULL,
    UNIQUE (company_id)
);
CREATE TRIGGER update_purchase_order_control_trg
    BEFORE UPDATE
    ON purchase_order_control
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();
CREATE INDEX purchase_order_control_company_id_idx ON purchase_order_control (company_id);
CREATE INDEX purchase_order_control_default_ap_status_type_id_idx ON purchase_order_control (default_account_payable_status_type_id);
CREATE INDEX purchase_order_control_default_vendor_id_idx ON purchase_order_control (default_vendor_id);
CREATE INDEX purchase_order_control_update_purchase_order_cost_id_idx ON purchase_order_control (update_purchase_order_cost_type_id);
CREATE INDEX purchase_order_control_default_purchase_order_type_id_idx ON purchase_order_control (default_purchase_order_type_id);
CREATE INDEX purchase_order_control_approval_required_flag_type_id_idx ON purchase_order_control (approval_required_flag_type_id);
