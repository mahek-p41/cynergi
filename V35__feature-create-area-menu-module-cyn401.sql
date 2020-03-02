CREATE TABLE area_type_domain (
	 id integer                                                                  NOT NULL PRIMARY KEY,
    name varchar(50) CHECK ( char_length(trim(name)) > 1)                       NOT NULL,
	 code varchar(10) CHECK ( char_length(trim(code)) > 1)                       NOT NULL,
	 description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
	 localization_code varchar(100) CHECK( char_length(trim(localization_code)) > 1) NOT NULL,
	 UNIQUE(name, code)
 );

COMMENT ON TABLE area_type_domain IS 'Domain table contains the individual areas/systems available for a company to implement. i.e. PO, AP, GL';

INSERT INTO area_type_domain (id,name,code,description,localization_code)
Values
(1,'ACCOUNT PAYABLE', 'AP', 'Account Payable Area and Functionality', 'account;.payable.area.and.functionality') ,
(2,'BANK RECONCILIATION', 'BR', 'Bank Reconciliation Area and Functionality', 'bank.reconciliation.area.and.functionality') ,
(3,'GENERAL LEDGER', 'GL', 'General Ledger Area and Functionality', 'general.ledger.area.and.functionality') ,
(4,'PURCHASE ORDER', 'PO', 'Purchase Order and Requistion Area and Functionality', 'purchase.order.and.requistion.area.and.functionality');


CREATE TABLE company_to_area (
   company_id BIGINT REFERENCES company(id)                                    NOT NULL,
   area_type_id  BIGINT REFERENCES area_type_domain(id)                        NOT NULL,
   UNIQUE(company_id,area_type_id)
);

COMMENT ON TABLE company_to_area IS 'Join table which joins the company to areas that the company has agreed to implement';


CREATE TABLE menu_type_domain(
    id     integer                                                              NOT NULL PRIMARY KEY,
    name varchar (50) CHECK ( char_length(trim(name)) > 1)                      NOT NULL,
    description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
    localization_code varchar(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (name)
);


CREATE TABLE module_type_domain(
    id     integer                                                             NOT NULL PRIMARY KEY,
    area_type_id BIGINT REFERENCES area_type_domain(id)                        NOT NULL,
    name varchar (50) CHECK ( char_length(trim(name)) > 1)                     NOT NULL,
    program varchar(50) CHECK ( char_length(trim(description)) > 1)            NOT NULL,
    description varchar (100) CHECK ( char_length(trim(description)) > 1)      NOT NULL,
    localization_code varchar(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (name,program)
);


CREATE TABLE menu_to_module (
   menu_type_id BIGINT REFERENCES menu_type_domain(id)                              NOT NULL,
   module_type_id  BIGINT REFERENCES module_type_domain(id)                         NOT NULL,
   UNIQUE(menu_type_id,module_type_id)
);

COMMENT ON TABLE menu_to_module IS 'Join table which joins the menus to the modules';

CREATE TABLE module_level (
   id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
   uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
   time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
   time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
   module_type_id BIGINT REFERENCES module_type_domain (id)                    NOT NULL,
   module_level integer                                                        NOT NULL,
   UNIQUE(module_type_id,module_level)
);


CREATE TRIGGER update_module_level_trg
   BEFORE UPDATE
   ON module_level
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
