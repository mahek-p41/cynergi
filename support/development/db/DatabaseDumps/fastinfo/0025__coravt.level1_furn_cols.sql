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
CREATE TABLE coravt.level1_furn_cols ( 
    id bigint NOT NULL, 
    furn_col_rec_type character varying(1), 
    furn_fab_rec_type character varying(1), 
    furn_col_code integer, 
    furn_fab_code integer, 
    furn_col_description_key character varying(51), 
    furn_col_description_rec_type character varying(1), 
    furn_fab_description_key character varying(51), 
    furn_fab_description_rec_type character varying(1), 
    furn_col_description character varying(50), 
    furn_fab_description character varying(50), 
    furn_col_primary_color_code integer, 
    furn_fab_primary_fabric_code integer, 
    furn_col_primary_color_indr character varying(1), 
    furn_fab_primary_fabric_indr character varying(1), 
    furn_col_last_changed_date date, 
    furn_fab_last_changed_date date, 
    created_at timestamp without time zone, 
    updated_at timestamp without time zone, 
    ht_etl_source_operation character varying(1) DEFAULT 'L'::character varying NOT NULL, 
    ht_etl_processing_stage integer DEFAULT 10 NOT NULL, 
    ht_etl_z_timestamp timestamp without time zone, 
    ht_etl_checksum character varying(32) 
); 
ALTER TABLE coravt.level1_furn_cols OWNER TO fastinfo_dba; 
CREATE SEQUENCE coravt.level1_furn_cols_id_seq 
    START WITH 1 
    INCREMENT BY 1 
    NO MINVALUE 
    NO MAXVALUE 
    CACHE 1; 
ALTER TABLE coravt.level1_furn_cols_id_seq OWNER TO fastinfo_dba; 
ALTER SEQUENCE coravt.level1_furn_cols_id_seq OWNED BY coravt.level1_furn_cols.id; 
ALTER TABLE ONLY coravt.level1_furn_cols ALTER COLUMN id SET DEFAULT nextval('coravt.level1_furn_cols_id_seq'::regclass); 
ALTER TABLE ONLY coravt.level1_furn_cols 
    ADD CONSTRAINT level1_furn_cols_pkey PRIMARY KEY (id); 
CREATE INDEX index_level1_furn_cols_on_ht_etl_processing_stage ON coravt.level1_furn_cols USING btree (ht_etl_processing_stage); 
