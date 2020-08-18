CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA fastinfo_prod_import;

CREATE TABLE fastinfo_prod_import.store_vw (
   id           BIGSERIAL                                             NOT NULL PRIMARY KEY,
   number       INTEGER,
   name         VARCHAR(27),
   dataset      VARCHAR(6)                                            NOT NULL,
   time_created TIMESTAMPTZ  DEFAULT clock_timestamp()                NOT NULL,
   time_updated TIMESTAMPTZ  DEFAULT clock_timestamp()                NOT NULL
);
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (1, 'KANSAS CITY', 'tstds1');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (3, 'INDEPENDENCE', 'tstds1');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (9000, 'HOME OFFICE', 'tstds1');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (1, 'Pelham Trading Post, Inc', 'tstds2');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (2, 'Camilla Trading Post, Inc.', 'tstds2');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (3, 'Arlington Trading Post', 'tstds2');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (4, 'Moultrie Trading Post, Inc', 'tstds2');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (5, 'Bainbridge Trading Post', 'tstds2');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (9000, 'HOME OFFICE', 'tstds2');

CREATE TABLE fastinfo_prod_import.department_vw (
    id               BIGSERIAL                              NOT NULL PRIMARY KEY,
    code             VARCHAR(2)                             NOT NULL,
    description      VARCHAR(12),
    dataset          VARCHAR(6)                             NOT NULL,
    time_created     TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    time_updated     TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL
);
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('AM', 'ASST MGR', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('AR', 'ACCOUNT REP', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('DE', 'DELIVERY DVR', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('EX', 'EXECUTIVE', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('MM', 'MARKET MGR', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('RM', 'REGIONAL MGR', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('SA', 'SALES ASSOC', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('SM', 'STORE MGR', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('TE', 'TERMINATED', 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('NO', null, 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('CY', null, 'tstds1');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('AM', 'ACCOUNT MGR', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('CO', 'COLLECTIONS', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('DE', 'DELIVERY', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('DM', 'DISTRICT MGR', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('MG', 'MANAGEMENT', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('OF', 'OFFICE', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('SA', 'SALES ASSOCI', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('SM', 'STORE MANAGE', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('WH', 'WAREHOUSE', 'tstds2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, dataset) VALUES ('HO', null, 'tstds2');


CREATE TABLE fastinfo_prod_import.employee_vw (
    id                          BIGINT                                           NOT NULL,
    number                      INTEGER      CHECK( number > 0 )                 NOT NULL,
    store_number                INTEGER                                          NOT NULL,
    dataset                     VARCHAR(6)   CHECK( char_length(dataset) = 6 )   NOT NULL,
    last_name                   VARCHAR(15)  CHECK( char_length(last_name) > 1 ) NOT NULL,
    first_name_mi               VARCHAR(15),
    pass_code                   VARCHAR(6)   CHECK( char_length(pass_code) > 0 ) NOT NULL,
    department                  VARCHAR(2),
    active                      BOOLEAN      DEFAULT TRUE                        NOT NULL,
    cynergi_system_admin        BOOLEAN      DEFAULT FALSE                       NOT NULL,
    alternative_store_indicator VARCHAR(1)   DEFAULT 'N'                         NOT NULL,
    alternative_area            INTEGER      DEFAULT 0                           NOT NULL,
    time_created  TIMESTAMPTZ                DEFAULT clock_timestamp()           NOT NULL,
    time_updated  TIMESTAMPTZ                DEFAULT clock_timestamp()           NOT NULL,
    UNIQUE(id, dataset)
);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (1 , 1, 3, 'tstds1', 'ROUTE 1', null, 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (2 , 2, 3, 'tstds1', 'ROUTE 2', null, 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (3 , 10, 3, 'tstds1', 'GOLD CUSTOMER', null, 'pass', 'NO', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (4 , 13, 3, 'tstds1', 'BANKRUPTCIES', null, 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (5 , 18, 3, 'tstds1', 'MANAGERS ACCTS', null, 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (6 , 19, 3, 'tstds1', 'SKIP', null, 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (7 , 20, 3, 'tstds1', 'LEGAL', null, 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (8 , 100, 1, 'tstds1', 'LATHERY', 'MARK', 'pass', 'RM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (9 , 101, 1, 'tstds1', 'TORRIJOS', 'MAGALI', 'pass', 'SM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (10, 102, 1, 'tstds1', 'VANDERPOOL', 'VICTORIA', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (11, 103, 1, 'tstds1', 'GERICKE', 'J KEITH', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (12, 104, 1, 'tstds1', 'CARRILLO', 'DIANA', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (13, 105, 1, 'tstds1', 'SPRAGUE', 'JOHN', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (14, 106, 3, 'tstds1', 'ROBB', 'DAKOTA', 'passwo', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (15, 107, 1, 'tstds1', 'CANTU', 'ENRIQUE', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (16, 108, 1, 'tstds1', 'THOMAS', 'BRIAN', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (17, 109, 1, 'tstds1', 'MACIAS', 'FERNANDO', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (18, 110, 1, 'tstds1', 'ROSARIO', 'JONATHAN', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (19, 111, 1, 'tstds1', 'MARTINEZ', 'DANIEL', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (20, 300, 1, 'tstds1', 'SEPULVEDA', 'DANIEL', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (21, 302, 3, 'tstds1', 'ADAME', 'JOSEPH', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (22, 303, 3, 'tstds1', 'MUNOZ', 'CHRISTOPHER', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (23, 304, 1, 'tstds1', 'RINEBERG', 'PHILIP', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (24, 305, 3, 'tstds1', 'TOLEFREE', 'JERMAINE', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (25, 306, 3, 'tstds1', 'THOMPSON', 'TYREE', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (26, 307, 3, 'tstds1', 'QUINONES', 'TONY', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (27, 308, 3, 'tstds1', 'LAY', 'KEVIN', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (28, 309, 3, 'tstds1', 'JONES', 'BRIAN', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (29, 310, 3, 'tstds1', 'PLUNKETT', 'RICHARD', 'pass', 'SM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (30, 311, 3, 'tstds1', 'JEWELL', 'CHRISTOPHER', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (31, 312, 1, 'tstds1', 'LATHERY', 'MONA', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (32, 313, 1, 'tstds1', 'JEWELL', 'CHRISTOPHER', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (33, 314, 3, 'tstds1', 'WADDY', 'ANN', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (34, 315, 3, 'tstds1', 'MUNOZA', 'CHRISTOPHER', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (35, 900, 9000, 'tstds1', 'RINEBERG', 'RICK', 'pass', 'EX', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (36, 901, 9000, 'tstds1', 'KULUVA', 'CHUCK', 'pass', 'EX', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (37, 9999, 9000, 'tstds1', 'ONLINE PAYMENTS', null, 'pass', 'CY', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (38, 90000, 1, 'tstds1', 'EXECUTIVE', null, 'pass', 'EX', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (39, 90001, 1, 'tstds1', 'REGIONAL MGR', null, 'pass', 'RM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (40, 90002, 1, 'tstds1', 'MARKET MANAGER', null, 'pass', 'MM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (41, 90003, 1, 'tstds1', 'STORE MANAGER', null, 'pass', 'SM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (42, 90004, 1, 'tstds1', 'ASST MANAGER', null, 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (43, 90005, 1, 'tstds1', 'ACCOUNT REP', null, 'pass', 'AR', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (44, 90006, 1, 'tstds1', 'SALES ASSOCIATE', null, 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (45, 90007, 1, 'tstds1', 'DELIVERY DRIVER', null, 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (46, 90008, 1, 'tstds1', 'TERMINATED', 'EMP', 'pass', 'TE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (47, 99998, 1, 'tstds1', '2-Way SMS', null, 'pass', 'RM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (1, 2, 1, 'tstds2', 'PALMER', 'L', 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (2, 4, 5, 'tstds2', 'MILLER', 'HEATHER NICOLE', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (3, 5, 4, 'tstds2', 'MARTINEZ', 'RUBEN', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (4, 6, 2, 'tstds2', 'BYNUM', 'DAVID', 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (5, 7, 1, 'tstds2', 'PALMER', 'DONNIE', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (6, 9, 1, 'tstds2', 'HURST', 'WANDA', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (7, 10, 3, 'tstds2', 'MCGLAMORY', 'PAMELA', 'pass', 'SM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (8, 11, 4, 'tstds2', 'MASONOFF', 'NICHOLAS', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (9, 12, 5, 'tstds2', 'CRANKFIELD', 'LADON M', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (10, 14, 1, 'tstds2', 'LEVERETTE', 'MATTHEW', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (11, 15, 4, 'tstds2', 'TAYLOR', 'DIANNE', 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (12, 17, 4, 'tstds2', 'MOORE', 'DANTE', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (13, 18, 2, 'tstds2', 'MOORE', 'FANTA', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (14, 21, 3, 'tstds2', 'GOWAN', 'LOGAN', 'pass', 'CO', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (15, 22, 1, 'tstds2', 'JACKSON', 'BRANTLEY', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (16, 23, 4, 'tstds2', 'JOHNSON', 'LIZA', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (17, 25, 1, 'tstds2', 'PALMER', 'AMY', 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (18, 26, 4, 'tstds2', 'ADAMS', 'KYLE', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (19, 30, 1, 'tstds2', 'PUZAKULICS', 'JESSICA', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (20, 31, 1, 'tstds2', 'PALMER', 'D.J.', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (21, 32, 1, 'tstds2', 'CHASTAIN', 'TARA', 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (22, 33, 4, 'tstds2', 'EMORY', 'JAMES', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (23, 38, 5, 'tstds2', 'PIPPIN', 'COREY', 'pass', 'CO', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (24, 40, 1, 'tstds2', 'MERRITT', 'ANTAVIUS', 'pass', 'DE', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (25, 50, 1, 'tstds2', 'PALMER', 'LINDA', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (26, 99, 1, 'tstds2', 'TEST', 'TEST', 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (27, 800, 1, 'tstds2', 'MANAGER', null, 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (28, 801, 1, 'tstds2', 'ACCOUNT MANAGER', null, 'pass', 'AM', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (29, 802, 1, 'tstds2', 'SALES ASSOC.', null, 'pass', 'SA', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (30, 901, 1, 'tstds2', 'ROUTE 1', null, 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (31, 902, 1, 'tstds2', 'ROUTE 2', null, 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (32, 903, 1, 'tstds2', 'ROUTE 3', null, 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (33, 904, 1, 'tstds2', 'ROUTE 4', null, 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (34, 905, 1, 'tstds2', 'ROUTE 5', null, 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (35, 906, 1, 'tstds2', 'ROUTE 6', null, 'pass', 'OF', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (36, 999, 1, 'tstds2', 'PAP', 'PAYMENT', 'pass', 'MG', true);
INSERT INTO fastinfo_prod_import.employee_vw(id, number, store_number, dataset, last_name, first_name_mi, pass_code, department, active) VALUES (37, 99998, 1, 'tstds2', '2-Way SMS', null, 'pass', 'HO', true);

CREATE TABLE fastinfo_prod_import.inventory_vw (
   id               BIGSERIAL                             NOT NULL PRIMARY KEY,
   dataset          VARCHAR(6)                            NOT NULL,
   serial_number    VARCHAR(10)                           NOT NULL,
   lookup_key       VARCHAR(20),
   lookup_key_type  VARCHAR(10)                           NOT NULL,
   barcode          VARCHAR(10)                           NOT NULL,
   alt_id           VARCHAR(30),
   brand            VARCHAR(30),
   model_number     VARCHAR(18)                           NOT NULL,
   product_code     TEXT                                  NOT NULL,
   description      VARCHAR(28),
   received_date    DATE,
   original_cost    NUMERIC(11,2)                         NOT NULL,
   actual_cost      NUMERIC(11,2)                         NOT NULL,
   model_category   VARCHAR(1)                            NOT NULL,
   times_rented     INTEGER                               NOT NULL,
   total_revenue    NUMERIC(11,2)                         NOT NULL,
   remaining_value  NUMERIC(11,2)                         NOT NULL,
   sell_price       NUMERIC(7,2)                          NOT NULL,
   assigned_value   NUMERIC(11,2)                         NOT NULL,
   idle_days        INTEGER                               NOT NULL,
   condition        VARCHAR(15),
   returned_date    DATE,
   location         INTEGER                               NOT NULL,
   status           VARCHAR(1)                            NOT NULL,
   primary_location INTEGER                               NOT NULL,
   location_type    INTEGER                               NOT NULL
);

COPY fastinfo_prod_import.inventory_vw(
   serial_number,
   lookup_key,
   lookup_key_type,
   barcode,
   alt_id,
   brand,
   model_number,
   product_code,
   description,
   received_date,
   original_cost,
   actual_cost,
   model_category,
   times_rented,
   total_revenue,
   remaining_value,
   sell_price,
   assigned_value,
   idle_days,
   condition,
   returned_date,
   location,
   status,
   primary_location,
   location_type,
   dataset
)
FROM '/tmp/fastinfo/test-inventory.csv' DELIMITER ',' CSV HEADER;
