CREATE TABLE freight_on_board_type_domain(
	 id integer                                                                  NOT NULL PRIMARY KEY,
    name varchar(50) CHECK ( char_length(trim(name)) > 1)                       NOT NULL,
	 code varchar(10) CHECK ( char_length(trim(code)) > 0)                       NOT NULL,
	 description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
	 localization_code varchar(100) CHECK( char_length(trim(localization_code)) > 1) NOT NULL,
	 UNIQUE(name, code)
 );

INSERT INTO freight_on_board_type_domain (id,name,code,description,localization_code)
Values
(1,'DESTINATION', 'D', 'Destination', 'destination') ,
(2,'SHIPPING', 'S', 'Shipping', 'shipping');

CREATE TABLE freight_calc_method_type_domain (
	 id integer                                                                  NOT NULL PRIMARY KEY,
    name varchar(50) CHECK ( char_length(trim(name)) > 1)                       NOT NULL,
	 code varchar(10) CHECK ( char_length(trim(code)) > 0)                       NOT NULL,
	 description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
	 localization_code varchar(100) CHECK( char_length(trim(localization_code)) > 1) NOT NULL,
	 UNIQUE(name, code)
 );

INSERT INTO freight_calc_method_type_domain(id,name,code,description,localization_code)
Values
(1,'ITEM', 'I', 'Item', 'item') ,
(2,'NONE', 'N', 'None', 'none'),
(3,'PERCENT', 'P', 'Percent', 'percent'),
(4,'SIZE', 'S', 'Size', 'size'),
(5,'WEIGHT', 'W', 'Weight', 'weight');

CREATE TABLE vendor_group (
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    company_id BIGINT REFERENCES company(id)                                    NOT NULL,
    code varchar (10) CHECK (char_length(trim(group_code)) > 1)                  NOT NULL,
    description varchar(50) CHECK  (char_length(trim(description)) > 1)         NOT NULL,
    UNIQUE(company_id, code)
);

CREATE TRIGGER update_vendor_group_trg
   BEFORE UPDATE
   ON vendor_group
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE TABLE vendor (
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    company_id BIGINT REFERENCES company(id)                                    NOT NULL,
    number integer CHECK ( number > 0 )                                         NOT NULL,
    name_key varchar(30) CHECK  (char_length(trim(name_key)) > 1)               NOT NULL,
    address_id  BIGINT REFERENCES address(id)                                   NOT NULL,
    our_account_number integer                                                  NOT NULL,
    pay_to_id BIGINT REFERENCES vendor(id),
    freight_on_board_type_id BIGINT REFERENCES freight_on_board_type_domain (id) NOT NULL,
    payment_terms_id BIGINT REFERENCES vendor_payment_term(id)                   NOT NULL,
    float_days integer,
    normal_days integer,
    return_policy BOOLEAN DEFAULT FALSE                                         NOT NULL,
    ship_via_id BIGINT REFERENCES ship_via(id)                                  NOT NULL,
    group_id BIGINT REFERENCES vendor_group(id)                                 NOT NULL,
    shutdown_from date,
    shutdown_thru date,
    minimum_quantity  integer,
    minimum_amount numeric(11,2),
    free_ship_quantity  integer,
    free_ship_amount numeric(11,2),
    vendor_1099 BOOLEAN DEFAULT FALSE                                           NOT NULL,
    federal_id_number varchar(12),
    sales_representative_name varchar(20),
    sales_representative_fax varchar (20),
    separate_check BOOLEAN DEFAULT FALSE                                        NOT NULL,
    bump_percent numeric(9,4),
    freight_calc_method_type_id BIGINT REFERENCES freight_calc_method_type_domain (id) NOT NULL,
    freight_percent numeric(8,3),
    freight_amount numeric(8,2),
    charge_inventory_tax_1 BOOLEAN DEFAULT FALSE                            NOT NULL,
    charge_inventory_tax_2 BOOLEAN DEFAULT FALSE                            NOT NULL,
    charge_inventory_tax_3 BOOLEAN DEFAULT FALSE                            NOT NULL,
    charge_inventory_tax_4 BOOLEAN DEFAULT FALSE                            NOT NULL,
    federal_id_number_verification BOOLEAN DEFAULT FALSE                    NOT NULL,
    email_address varchar(320),
    UNIQUE (company_id, number)
);

CREATE TRIGGER update_vendor_trg
   BEFORE UPDATE
   ON vendor
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX idx_company_id
ON vendor(company_id);

CREATE INDEX idx_address_id
ON vendor(address_id);

CREATE INDEX idx_freight_on_board_type_id
ON vendor(freight_on_board_type_id);

CREATE INDEX idx_ship_via_id
ON vendor(ship_via_id);

CREATE INDEX idx_group_id
ON vendor(group_id);

CREATE INDEX idx_freight_calc_method_type_id
ON vendor(freight_calc_method_type_id);


