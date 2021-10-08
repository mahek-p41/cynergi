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
CREATE TABLE coravt.level2_models ( 
    id bigint NOT NULL, 
    itemfile_nbr character varying(18), 
    manufacturer_id bigint, 
    row_creator_id bigint, 
    created_at timestamp without time zone, 
    updated_at timestamp without time zone, 
    itemfile_nbr_first_1 character varying(1), 
    itemfile_nbr_first_2 character varying(2), 
    itemfile_1prod_2manuf character varying(2), 
    itemfile_1prod_3manuf character varying(3), 
    itemfile_mfg_code character varying(3), 
    itemfile_nbr_2_and_3 character varying(2), 
    itemfile_2prod_2manuf character varying(2), 
    itemfile_2prod_3manuf character varying(3), 
    itemfile_model_nbr_4_18 character varying(15), 
    itemfile_mfg_model_nbr character varying(14), 
    itemfile_model_nbr_5_18 character varying(14), 
    itemfile_model_nbr_6_18 character varying(13), 
    itemfile_desc_1 character varying(28), 
    itemfile_desc_1_and_2 character varying(54), 
    itemfile_desc_2 character varying(26), 
    itemfile_days_out_of_stock integer, 
    itemfile_nbr_pieces integer, 
    itemfile_reg_price numeric(11,3), 
    itemfile_sale_price numeric(11,3), 
    itemfile_spiff numeric(11,2), 
    itemfile_wholesale_tax_percent numeric(11,2), 
    itemfile_average_cost numeric(11,3), 
    itemfile_last_cost numeric(11,3), 
    itemfile_commission_code integer, 
    itemfile_furn_style character varying(2), 
    itemfile_special_fee_item character varying(1), 
    itemfile_gl_main_acct_1 integer, 
    itemfile_gl_sub_acct_1 integer, 
    itemfile_gl_main_acct_2 integer, 
    itemfile_gl_sub_acct_2 integer, 
    itemfile_gl_main_acct_3 integer, 
    itemfile_gl_sub_acct_3 integer, 
    itemfile_gl_main_acct_4 integer, 
    itemfile_gl_sub_acct_4 integer, 
    itemfile_gl_main_acct_5 integer, 
    itemfile_gl_sub_acct_5 integer, 
    itemfile_gl_main_acct_6 integer, 
    itemfile_gl_sub_acct_6 integer, 
    itemfile_gl_main_acct_7 integer, 
    itemfile_gl_sub_acct_7 integer, 
    itemfile_use_store_as_pc_indr character varying(1), 
    itemfile_serialized_indr character varying(1), 
    itemfile_cost_method_indr integer, 
    vendor_id bigint DEFAULT 0 NOT NULL, 
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL, 
    itemfile_percent_of_price numeric(11,2), 
    itemfile_qty_on_hand integer, 
    itemfile_sell_service_pol_indr character varying(1), 
    itemfile_taxable_indr character varying(1), 
    itemfile_movie_type_indr character varying(1), 
    itemfile_rtr_pricing_type character varying(1), 
    itemfile_rto_spiff numeric(11,2), 
    itemfile_rtr_monthly_price numeric(11,2), 
    itemfile_normal_terms_monthly integer, 
    itemfile_monthly_rto_price numeric(11,2), 
    itemfile_normal_terms_weekly integer, 
    itemfile_weekly_rto_price numeric(11,2), 
    itemfile_discontinued_indr character varying(1), 
    itemfile_first_received_date date, 
    itemfile_first_received_year integer, 
    itemfile_first_received_month integer, 
    itemfile_first_received_day integer, 
    itemfile_furniture_indr character varying(1), 
    itemfile_qty_on_order integer, 
    itemfile_date_due_in date, 
    itemfile_last_changed_date date, 
    itemfile_book_value_nbr_months integer, 
    itemfile_inv_cube_size integer, 
    itemfile_inv_unit_weight integer, 
    itemfile_upc_code character varying(20), 
    itemfile_min_accept_price numeric(11,2), 
    itemfile_mgrs_override_price numeric(11,2), 
    itemfile_list_price numeric(11,2), 
    itemfile_no_discount_sale_item character varying(1), 
    itemfile_rate_code character varying(6), 
    itemfile_freight_override_amt numeric(11,2), 
    itemfile_glacct_rentinv_main integer, 
    itemfile_glacct_rentinv_sub integer, 
    itemfile_glacct_pfydepadj_main integer, 
    itemfile_glacct_pfydepadj_sub integer, 
    itemfile_glacct_accumdep_main integer, 
    itemfile_glacct_accumdep_sub integer, 
    itemfile_recv_inv_status character varying(1) 
); 
ALTER TABLE coravt.level2_models OWNER TO postgres; 
CREATE SEQUENCE coravt.level2_models_id_seq 
    START WITH 1 
    INCREMENT BY 1 
    NO MINVALUE 
    NO MAXVALUE 
    CACHE 1; 
ALTER TABLE coravt.level2_models_id_seq OWNER TO postgres; 
ALTER SEQUENCE coravt.level2_models_id_seq OWNED BY coravt.level2_models.id; 
ALTER TABLE ONLY coravt.level2_models ALTER COLUMN id SET DEFAULT nextval('coravt.level2_models_id_seq'::regclass); 
ALTER TABLE ONLY coravt.level2_models 
    ADD CONSTRAINT level2_models_pkey PRIMARY KEY (id); 
CREATE UNIQUE INDEX index_level2_models_on_itemfile_nbr ON coravt.level2_models USING btree (itemfile_nbr); 
CREATE INDEX index_level2_models_on_itemfile_upc_code ON coravt.level2_models USING btree (itemfile_upc_code); 
CREATE INDEX index_level2_models_on_manufacturer_id ON coravt.level2_models USING btree (manufacturer_id); 
CREATE INDEX index_level2_models_on_row_creator_id ON coravt.level2_models USING btree (row_creator_id); 
CREATE INDEX index_level2_models_on_vendor_id ON coravt.level2_models USING btree (vendor_id); 
