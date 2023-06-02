CREATE TABLE security_access_point_type_domain
(
    id                INTEGER                                 NOT NULL PRIMARY KEY,
    value             VARCHAR(100)                            NOT NULL,
    description       VARCHAR(100)                            NOT NULL,
    localization_code VARCHAR(100)                            NOT NULL,
    area_id bigint REFERENCES area_type_domain(id)
);

COMMENT ON COLUMN security_access_point_type_domain.value
    IS 'Most Cases This represents the Z program name.';

CREATE INDEX security_access_point_type_domain_area_id_idx ON security_access_point_type_domain (area_id);

-- This table is replacing the department table in Cynergi

CREATE TABLE security_group (
    id              UUID         DEFAULT uuid_generate_v1()                 NOT NULL PRIMARY KEY,
    time_created    TIMESTAMPTZ  DEFAULT clock_timestamp()                  NOT NULL,
    time_updated    TIMESTAMPTZ  DEFAULT clock_timestamp()                  NOT NULL,
    key             VARCHAR(100) CHECK ( char_length(key) > 1 )             NOT NULL,
    description     VARCHAR(100) CHECK ( char_length(description) > 1 )     NOT NULL,
    company_id      UUID       REFERENCES company (id)                    NOT NULL
);

CREATE TRIGGER update_security_group_trg
    BEFORE UPDATE
    ON security_group
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX security_group_company_id_idx ON security_group (company_id);

-- This table is a join table sgtsap

CREATE TABLE security_group_to_security_access_point (
      security_group_id          UUID     REFERENCES security_group (id)                       NOT NULL,
      security_access_point_id   BIGINT   REFERENCES security_access_point_type_domain (id)    NOT NULL
);

CREATE INDEX sgtsap_security_group_id_idx ON security_group_to_security_access_point(security_group_id);
CREATE INDEX sgtsap_security_access_point_id_idx ON security_group_to_security_access_point(security_access_point_id);

-- This table is a join table using the employee_id_sfk which uses the fastinfo materalized view, system_employees_fimvw

CREATE TABLE employee_to_security_group (
      employee_id_sfk     INTEGER                                          NOT NULL,
      security_group_id   UUID   REFERENCES security_group (id)            NOT NULL
);

CREATE INDEX employee_to_security_group_id_idx ON security_group(id);
