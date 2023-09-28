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
CREATE TABLE coravt.level2_location_types ( 
    id bigint NOT NULL, 
    location_type_code integer, 
    descr character varying(30), 
    created_at timestamp without time zone, 
    updated_at timestamp without time zone, 
    row_creator_id bigint, 
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL 
); 
ALTER TABLE coravt.level2_location_types OWNER TO fastinfo_dba; 
CREATE SEQUENCE coravt.level2_location_types_id_seq 
    START WITH 1 
    INCREMENT BY 1 
    NO MINVALUE 
    NO MAXVALUE 
    CACHE 1; 
ALTER TABLE coravt.level2_location_types_id_seq OWNER TO fastinfo_dba; 
ALTER SEQUENCE coravt.level2_location_types_id_seq OWNED BY coravt.level2_location_types.id; 
ALTER TABLE ONLY coravt.level2_location_types ALTER COLUMN id SET DEFAULT nextval('coravt.level2_location_types_id_seq'::regclass); 
ALTER TABLE ONLY coravt.level2_location_types 
    ADD CONSTRAINT level2_location_types_pkey PRIMARY KEY (id); 
CREATE UNIQUE INDEX index_level2_location_types_on_location_type_code ON coravt.level2_location_types USING btree (location_type_code); 
CREATE INDEX index_level2_location_types_on_row_creator_id ON coravt.level2_location_types USING btree (row_creator_id); 
