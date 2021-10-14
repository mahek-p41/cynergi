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
CREATE TABLE corrto.level2_manufacturers ( 
    id bigint NOT NULL, 
    manu_code character varying(3), 
    manufile_manu_name character varying(28), 
    created_at timestamp without time zone, 
    updated_at timestamp without time zone, 
    row_creator_id bigint, 
    manufile_manu_code_an2 character varying(2), 
    manufile_manu_code_an3 character varying(3), 
    packed_cost_pcode_1 character varying(2), 
    packed_cost_percent_1 numeric(11,3), 
    packed_cost_pcode_2 character varying(2), 
    packed_cost_percent_2 numeric(11,3), 
    packed_cost_pcode_3 character varying(2), 
    packed_cost_percent_3 numeric(11,3), 
    packed_cost_pcode_4 character varying(2), 
    packed_cost_percent_4 numeric(11,3), 
    packed_cost_pcode_5 character varying(2), 
    packed_cost_percent_5 numeric(11,3), 
    packed_cost_pcode_6 character varying(2), 
    packed_cost_percent_6 numeric(11,3), 
    packed_cost_pcode_7 character varying(2), 
    packed_cost_percent_7 numeric(11,3), 
    packed_cost_pcode_8 character varying(2), 
    packed_cost_percent_8 numeric(11,3), 
    packed_cost_pcode_9 character varying(2), 
    packed_cost_percent_9 numeric(11,3), 
    packed_cost_pcode_10 character varying(2), 
    packed_cost_percent_10 numeric(11,3), 
    manufile_warranty_code_1 character varying(2), 
    manufile_warranty_desc_1 character varying(15), 
    manufile_warranty_length_1 integer, 
    manufile_warranty_period_1 character varying(1), 
    manufile_warranty_code_2 character varying(2), 
    manufile_warranty_desc_2 character varying(15), 
    manufile_warranty_length_2 integer, 
    manufile_warranty_period_2 character varying(1), 
    manufile_warranty_code_3 character varying(2), 
    manufile_warranty_desc_3 character varying(15), 
    manufile_warranty_length_3 integer, 
    manufile_warranty_period_3 character varying(1), 
    manufile_warranty_code_4 character varying(2), 
    manufile_warranty_desc_4 character varying(15), 
    manufile_warranty_length_4 integer, 
    manufile_warranty_period_4 character varying(1), 
    manufile_warranty_code_5 character varying(2), 
    manufile_warranty_desc_5 character varying(15), 
    manufile_warranty_length_5 integer, 
    manufile_warranty_period_5 character varying(1), 
    manufile_warranty_code_6 character varying(2), 
    manufile_warranty_desc_6 character varying(15), 
    manufile_warranty_length_6 integer, 
    manufile_warranty_period_6 character varying(1), 
    manufile_warranty_code_7 character varying(2), 
    manufile_warranty_desc_7 character varying(15), 
    manufile_warranty_length_7 integer, 
    manufile_warranty_period_7 character varying(1), 
    manufile_warranty_code_8 character varying(2), 
    manufile_warranty_desc_8 character varying(15), 
    manufile_warranty_length_8 integer, 
    manufile_warranty_period_8 character varying(1), 
    manufile_warranty_code_9 character varying(2), 
    manufile_warranty_desc_9 character varying(15), 
    manufile_warranty_length_9 integer, 
    manufile_warranty_period_9 character varying(1), 
    manufile_warranty_code_10 character varying(2), 
    manufile_warranty_desc_10 character varying(15), 
    manufile_warranty_length_10 integer, 
    manufile_warranty_period_10 character varying(1), 
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL 
); 
ALTER TABLE corrto.level2_manufacturers OWNER TO postgres; 
CREATE SEQUENCE corrto.level2_manufacturers_id_seq 
    START WITH 1 
    INCREMENT BY 1 
    NO MINVALUE 
    NO MAXVALUE 
    CACHE 1; 
ALTER TABLE corrto.level2_manufacturers_id_seq OWNER TO postgres; 
ALTER SEQUENCE corrto.level2_manufacturers_id_seq OWNED BY corrto.level2_manufacturers.id; 
ALTER TABLE ONLY corrto.level2_manufacturers ALTER COLUMN id SET DEFAULT nextval('corrto.level2_manufacturers_id_seq'::regclass); 
ALTER TABLE ONLY corrto.level2_manufacturers 
    ADD CONSTRAINT level2_manufacturers_pkey PRIMARY KEY (id); 
CREATE UNIQUE INDEX index_level2_manufacturers_on_manu_code ON corrto.level2_manufacturers USING btree (manu_code); 
CREATE INDEX index_level2_manufacturers_on_row_creator_id ON corrto.level2_manufacturers USING btree (row_creator_id); 
