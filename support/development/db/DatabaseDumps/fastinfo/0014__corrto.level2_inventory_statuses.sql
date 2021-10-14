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
CREATE TABLE corrto.level2_inventory_statuses ( 
    id bigint NOT NULL, 
    status_code character varying(1), 
    descr character varying(30), 
    active_flag character varying(1), 
    created_at timestamp without time zone, 
    updated_at timestamp without time zone, 
    row_creator_id bigint, 
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL 
); 
ALTER TABLE corrto.level2_inventory_statuses OWNER TO postgres; 
CREATE SEQUENCE corrto.level2_inventory_statuses_id_seq 
    START WITH 1 
    INCREMENT BY 1 
    NO MINVALUE 
    NO MAXVALUE 
    CACHE 1; 
ALTER TABLE corrto.level2_inventory_statuses_id_seq OWNER TO postgres; 
ALTER SEQUENCE corrto.level2_inventory_statuses_id_seq OWNED BY corrto.level2_inventory_statuses.id; 
ALTER TABLE ONLY corrto.level2_inventory_statuses ALTER COLUMN id SET DEFAULT nextval('corrto.level2_inventory_statuses_id_seq'::regclass); 
ALTER TABLE ONLY corrto.level2_inventory_statuses 
    ADD CONSTRAINT level2_inventory_statuses_pkey PRIMARY KEY (id); 
CREATE INDEX index_level2_inventory_statuses_on_row_creator_id ON corrto.level2_inventory_statuses USING btree (row_creator_id); 
CREATE UNIQUE INDEX index_level2_inventory_statuses_on_status_code ON corrto.level2_inventory_statuses USING btree (status_code); 
