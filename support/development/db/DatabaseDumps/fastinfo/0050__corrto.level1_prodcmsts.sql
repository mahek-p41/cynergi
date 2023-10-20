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
CREATE TABLE corrto.level1_prodcmsts (
    id bigint NOT NULL,
    prodcmst_class_code character varying(1),
    prodcmst_class_desc character varying(15),
    prodcmst_sl_bk_start_date date,
    prodcmst_sl_bk_end_date date,
    prodcmst_if_bk_start_date date,
    prodcmst_if_bk_end_date date,
    prodcmst_macrs_bk_start_date date,
    prodcmst_macrs_bk_end_date date,
    prodcmst_macrs_tax_start_date date,
    prodcmst_macrs_tax_end_date date,
    prodcmst_rent_sw character varying(1),
    prodcmst_transition_into_sw character varying(1),
    prodcmst_transition_out_of_sw character varying(1),
    prodcmst_cash_sale_sw character varying(1),
    prodcmst_allow_depr_sw character varying(1),
    prodcmst_allow_sl_life_sw character varying(1),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    ht_etl_source_operation character varying(1) DEFAULT 'L'::character varying NOT NULL,
    ht_etl_processing_stage integer DEFAULT 10 NOT NULL,
    ht_etl_z_timestamp timestamp without time zone,
    ht_etl_checksum character varying(32)
);
ALTER TABLE corrto.level1_prodcmsts OWNER TO fastinfo_dba;
CREATE SEQUENCE corrto.level1_prodcmsts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE corrto.level1_prodcmsts_id_seq OWNER TO fastinfo_dba;
ALTER SEQUENCE corrto.level1_prodcmsts_id_seq OWNED BY corrto.level1_prodcmsts.id;
ALTER TABLE ONLY corrto.level1_prodcmsts ALTER COLUMN id SET DEFAULT nextval('corrto.level1_prodcmsts_id_seq'::regclass);
ALTER TABLE ONLY corrto.level1_prodcmsts
    ADD CONSTRAINT level1_prodcmsts_pkey PRIMARY KEY (id);
CREATE INDEX index_level1_prodcmsts_on_ht_etl_processing_stage ON corrto.level1_prodcmsts USING btree (ht_etl_processing_stage) WHERE (ht_etl_processing_stage < 10);
