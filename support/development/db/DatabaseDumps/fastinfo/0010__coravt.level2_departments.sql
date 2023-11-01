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
CREATE TABLE coravt.level2_departments ( 
    id bigint NOT NULL, 
    loc_dept_code character varying(2), 
    loc_dept_desc character varying(12), 
    loc_dept_security_profile integer, 
    loc_dept_default_menu character varying(8), 
    created_at timestamp without time zone, 
    updated_at timestamp without time zone, 
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL 
); 
ALTER TABLE coravt.level2_departments OWNER TO fastinfo_dba; 
CREATE SEQUENCE coravt.level2_departments_id_seq 
    START WITH 1 
    INCREMENT BY 1 
    NO MINVALUE 
    NO MAXVALUE 
    CACHE 1; 
ALTER TABLE coravt.level2_departments_id_seq OWNER TO fastinfo_dba; 
ALTER SEQUENCE coravt.level2_departments_id_seq OWNED BY coravt.level2_departments.id; 
ALTER TABLE ONLY coravt.level2_departments ALTER COLUMN id SET DEFAULT nextval('coravt.level2_departments_id_seq'::regclass); 
ALTER TABLE ONLY coravt.level2_departments 
    ADD CONSTRAINT level2_departments_pkey PRIMARY KEY (id); 
