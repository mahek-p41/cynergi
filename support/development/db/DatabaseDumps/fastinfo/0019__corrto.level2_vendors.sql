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
CREATE TABLE corrto.level2_vendors ( 
    id bigint NOT NULL, 
    vendor_term_id bigint DEFAULT 0 NOT NULL, 
    vend_number integer NOT NULL, 
    vend_company2 integer, 
    vend_name_key character varying(30), 
    vend_address bigint, 
    vend_address_3 integer, 
    vend_our_acct_number bigint, 
    vend_pay_to integer, 
    vend_buyer character varying(8), 
    vend_fob character varying(1), 
    vend_float_days integer, 
    vend_norm_days integer, 
    vend_return_policy character varying(1), 
    vend_next_ap bigint, 
    vend_ship_via integer, 
    vend_run_id character varying(8), 
    vend_shutdown_from integer, 
    vend_shutdown_thru integer, 
    vend_minimum_qty integer, 
    vend_minimum_amt numeric(11,2), 
    vend_free_ship_qty integer, 
    vend_free_ship_amt numeric(11,2), 
    vend_type character varying(3), 
    vend_1099 character varying(1), 
    vend_fin character varying(12), 
    vend_our_acct_nbr_an character varying(9), 
    vend_ytd_purchases numeric(11,2), 
    vend_ly_purchases numeric(11,2), 
    vend_balance numeric(11,2), 
    vend_ytd_discounts numeric(11,2), 
    vend_last_payment integer, 
    vend_account_number character varying(20), 
    vend_sales_rep_name character varying(20), 
    vend_sales_rep_fax bigint, 
    vend_separate_check character varying(1), 
    vend_country_code character varying(3), 
    vend_bump_percent numeric(11,4), 
    vend_freight_calc_method character varying(1), 
    vend_freight_percent numeric(11,3), 
    vend_freight_amount numeric(11,2), 
    vend_rebate_code_1 integer, 
    vend_rebate_code_2 integer, 
    vend_rebate_code_3 integer, 
    vend_rebate_code_4 integer, 
    vend_rebate_code_5 integer, 
    vend_chg_inv_tax_1 character varying(1), 
    vend_chg_inv_tax_2 character varying(1), 
    vend_chg_inv_tax_3 character varying(1), 
    vend_chg_inv_tax_4 character varying(1), 
    created_at timestamp without time zone, 
    updated_at timestamp without time zone, 
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL 
); 
ALTER TABLE corrto.level2_vendors OWNER TO fastinfo_dba; 
CREATE SEQUENCE corrto.level2_vendors_id_seq 
    START WITH 1 
    INCREMENT BY 1 
    NO MINVALUE 
    NO MAXVALUE 
    CACHE 1; 
ALTER TABLE corrto.level2_vendors_id_seq OWNER TO fastinfo_dba; 
ALTER SEQUENCE corrto.level2_vendors_id_seq OWNED BY corrto.level2_vendors.id; 
ALTER TABLE ONLY corrto.level2_vendors ALTER COLUMN id SET DEFAULT nextval('corrto.level2_vendors_id_seq'::regclass); 
ALTER TABLE ONLY corrto.level2_vendors 
    ADD CONSTRAINT level2_vendors_pkey PRIMARY KEY (id); 
CREATE UNIQUE INDEX index_level2_vendors_on_vend_number ON corrto.level2_vendors USING btree (vend_number); 
