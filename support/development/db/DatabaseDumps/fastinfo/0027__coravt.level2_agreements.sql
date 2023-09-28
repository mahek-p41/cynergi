SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET search_path = coravt, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = false;
CREATE TABLE level2_agreements (
    id bigint NOT NULL,
    agreement_number bigint,
    customer_id bigint,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    row_creator_id bigint,
    agreement_type character varying(2),
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL
);
ALTER TABLE coravt.level2_agreements OWNER TO postgres;
CREATE SEQUENCE level2_agreements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE coravt.level2_agreements_id_seq OWNER TO postgres;
ALTER SEQUENCE level2_agreements_id_seq OWNED BY level2_agreements.id;
ALTER TABLE ONLY level2_agreements ALTER COLUMN id SET DEFAULT nextval('level2_agreements_id_seq'::regclass);
ALTER TABLE ONLY level2_agreements
    ADD CONSTRAINT level2_agreements_pkey PRIMARY KEY (id);
CREATE INDEX index_level2_agreements_on_customer_id ON level2_agreements USING btree (customer_id);
CREATE INDEX index_level2_agreements_on_row_creator_id ON level2_agreements USING btree (row_creator_id);
CREATE UNIQUE INDEX index_level2_agreements_unique_constraint ON level2_agreements USING btree (agreement_number, customer_id);

