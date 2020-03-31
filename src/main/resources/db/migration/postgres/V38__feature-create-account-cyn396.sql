CREATE TABLE account_type_domain (
	 id integer                                                                  NOT NULL PRIMARY KEY,
    name varchar(50) CHECK ( char_length(trim(name)) > 1)                       NOT NULL,
	 value varchar(10) CHECK ( char_length(trim(value)) > 0)                     NOT NULL,
	 description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
	 localization_code varchar(100) CHECK( char_length(trim(localization_code)) > 1) NOT NULL,
	 UNIQUE(value)
 );

INSERT INTO account_type_domain(id,name,value,description,localization_code)
Values
(1,'ASSET', 'A', 'Asset Account', 'asset.account') ,
(2,'CAPITAL', 'C', 'Capital Account', 'capital.account'),
(3,'EXPENSE', 'E', 'Expense Account', 'expense.account') ,
(4,'LIABILITY', 'L', 'Liability Account', 'liability.account'),
(5,'REVENUE', 'R', 'Revenue Account', 'revenue.account');

CREATE TABLE normal_account_balance_type_domain (
	 id integer                                                                  NOT NULL PRIMARY KEY,
    value varchar(10) CHECK ( char_length(trim(value)) > 0)                     NOT NULL,
	 description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
	 localization_code varchar(100) CHECK( char_length(trim(localization_code)) > 1) NOT NULL,
	 UNIQUE(value)
 );

INSERT INTO normal_account_balance_type_domain(id,value,description,localization_code)
Values
(1,'C', 'Credit', 'credit') ,
(2,'D', 'debit', 'debit');

CREATE TABLE status_type_domain (
	 id integer                                                                  NOT NULL PRIMARY KEY,
    value varchar(10) CHECK ( char_length(trim(value)) > 0)                     NOT NULL,
	 description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
	 localization_code varchar(100) CHECK( char_length(trim(localization_code)) > 1) NOT NULL,
	 UNIQUE(value)
 );

INSERT INTO status_type_domain(id,value,description,localization_code)
Values
(1,'A', 'Active', 'active') ,
(2,'I', 'inactive', 'inactive');

CREATE TABLE bank_currency_code_type_domain (
	 id integer                                                                  NOT NULL PRIMARY KEY,
    value varchar(10) CHECK ( char_length(trim(value)) > 0)                     NOT NULL,
	 description varchar(100) CHECK ( char_length(trim(description)) > 1)        NOT NULL,
	 localization_code varchar(100) CHECK( char_length(trim(localization_code)) > 1) NOT NULL,
	 UNIQUE(value)
 );

INSERT INTO bank_currency_code_type_domain(id,value,description,localization_code)
Values
(1,'USA', 'United States', 'united.states') ,
(2,'CAN', 'Canada', 'Canada');


CREATE TABLE bank (
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    company_id BIGINT REFERENCES company(id)                                    NOT NULL,
    address_id BIGINT REFERENCES address(id)                                    NOT NULL,
    number  INTEGER CHECK( number > 0 )                                         NOT NULL,
    name varchar(50) CHECK ( char_length(trim(name)) > 1)                       NOT NULL,
    general_ledger_profit_center_sfk INTEGER CHECK( general_ledger_profit_center_sfk > 0 ) NOT NULL, --profit center is store
    account_balance numeric(13,2), --Input the bank account balance for check reconciliation
    account_number INTEGER CHECK( account_number > 0 )                          NOT NULL, --Input the bank account number
    currency_code_id BIGINT REFERENCES bank_currency_code_type_domain(id) NOT NULL,
    UNIQUE (company_id, number )
);

 CREATE TRIGGER update_bank_trg
   BEFORE UPDATE
   ON bank
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE INDEX idx_bank_company_id
    ON bank (company_id);

CREATE INDEX idx_bank_address_id
    ON bank (address_id);


CREATE TABLE account (
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    company_id BIGINT REFERENCES company(id)                                    NOT NULL,
    number  INTEGER CHECK( number > 0 )                                         NOT NULL,
    description varchar(30) CHECK ( char_length(trim(description)) > 1)         NOT NULL,
    type_id BIGINT REFERENCES account_type_domain (id)                             NOT NULL,
    normal_account_balance_type_id BIGINT REFERENCES normal_account_balance_type_domain(id) NOT NULL,
    status_type_id  BIGINT REFERENCES status_type_domain(id)                    NOT NULL,
    account_balance_forward numeric(14,2),
    form_1099_field  integer, -- field # on the 1099 form for this account
    bank_indicator BOOLEAN DEFAULT FALSE                                    NOT NULL,
    bank_number_id BIGINT REFERENCES bank(id),
    corporate_account_indicator BOOLEAN DEFAULT FALSE                       NOT NULL,
    UNIQUE (company_id, number )
);

CREATE TRIGGER update_account_trg
   BEFORE UPDATE
   ON account
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE INDEX idx_account_company_id
    ON account (company_id);

CREATE INDEX idx_account_type_id
    ON account (type_id);

CREATE INDEX idx_account_status_type_id
    ON account (status_type_id);

 CREATE INDEX idx_account_bank_number_id
    ON account (bank_number_id);


ALTER TABLE bank
ADD column gl_account_id BIGINT REFERENCES account(id)                                    NOT NULL;

CREATE INDEX idx_bank_gl_account_id
    ON bank (gl_account_id);

