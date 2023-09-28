SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;
SET default_tablespace = '';
SET default_table_access_method = heap;
CREATE TABLE cortst.level1_operators (
    id bigint NOT NULL,
    operator_type character varying(1),
    operator_name character varying(8),
    operator_location character varying(30),
    operator_security_1 smallint,
    operator_security_2 smallint,
    operator_security_3 smallint,
    operator_security_4 smallint,
    operator_security_5 smallint,
    operator_security_6 smallint,
    operator_security_7 smallint,
    operator_security_8 smallint,
    operator_security_9 smallint,
    operator_security_10 smallint,
    operator_security_11 smallint,
    operator_security_12 smallint,
    operator_security_13 smallint,
    operator_security_14 smallint,
    operator_security_15 smallint,
    operator_security_16 smallint,
    operator_date timestamp without time zone,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    ht_etl_source_operation character varying(1) DEFAULT 'L'::character varying NOT NULL,
    ht_etl_processing_stage integer DEFAULT 10 NOT NULL,
    ht_etl_z_timestamp timestamp without time zone,
    ht_etl_checksum character varying(32)
);
ALTER TABLE cortst.level1_operators OWNER TO fastinfo_dba;
CREATE SEQUENCE cortst.level1_operators_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE cortst.level1_operators_id_seq OWNER TO fastinfo_dba;
ALTER SEQUENCE cortst.level1_operators_id_seq OWNED BY cortst.level1_operators.id;
ALTER TABLE ONLY cortst.level1_operators ALTER COLUMN id SET DEFAULT nextval('cortst.level1_operators_id_seq'::regclass);
ALTER TABLE ONLY cortst.level1_operators
    ADD CONSTRAINT level1_operators_pkey PRIMARY KEY (id);
CREATE INDEX index_level1_operators_on_ht_etl_processing_stage ON cortst.level1_operators USING btree (ht_etl_processing_stage) WHERE (ht_etl_processing_stage < 10);
