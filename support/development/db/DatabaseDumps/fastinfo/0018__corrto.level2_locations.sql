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
CREATE TABLE corrto.level2_locations ( 
    id bigint NOT NULL, 
    store_id bigint, 
    location_type_id bigint, 
    loc_tran_loc integer, 
    loc_transfer_desc character varying(27), 
    loc_transfer_loc_group_nbr_1 integer, 
    loc_transfer_loc_group_nbr_2 integer, 
    loc_transfer_loc_group_nbr_3 integer, 
    loc_transfer_loc_group_nbr_4 integer, 
    loc_transfer_loc_group_nbr_5 integer, 
    loc_transfer_loc_group_nbr_6 integer, 
    loc_transfer_loc_group_nbr_7 integer, 
    loc_transfer_loc_group_nbr_8 integer, 
    loc_transfer_loc_group_nbr_9 integer, 
    loc_transfer_loc_group_nbr_10 integer, 
    loc_transfer_loc_group_desc character varying(21), 
    loc_tran_currency_convert_rate numeric(15,4), 
    loc_tran_master_nbr integer, 
    loc_tran_retail_to_rental_loc character varying(1), 
    loc_tran_machine_nbr integer, 
    loc_tran_data_phone bigint, 
    loc_tran_voice_phone bigint, 
    loc_tran_strip_dir character varying(22), 
    loc_tran_stripped_indr character varying(1), 
    loc_tran_lowest_movie_copy_nbr integer, 
    loc_tran_seperate_system_indr character varying(1), 
    loc_tran_send_strip_directory character varying(22), 
    loc_tran_market_nbr integer, 
    loc_tran_region_nbr integer, 
    loc_tran_market_name character varying(27), 
    loc_tran_region_name character varying(27), 
    loc_tran_company_nbr integer, 
    loc_tran_company_name character varying(27), 
    loc_tran_subtract_petty_cash_i integer, 
    loc_tran_add_sales_to_movies_i character varying(1), 
    loc_tran_bld_sum_this_loc_indr character varying(1), 
    loc_tran_address character varying(24), 
    loc_tran_city character varying(15), 
    loc_tran_state character varying(2), 
    loc_tran_zip integer, 
    loc_tran_zip_plus integer, 
    loc_tran_last_changed_date integer, 
    loc_tran_active_store_indr character varying(1), 
    loc_tran_country_code character varying(3), 
    loc_tran_zip_pc character varying(6), 
    loc_tran_inventory_price_grp character varying(4), 
    loc_tran_date_store_opened integer, 
    loc_tran_rmte_pmt_store_indr character varying(1), 
    loc_tran_inv_max bigint, 
    loc_tran_time_zone character varying(3), 
    loc_tran_dest_based_tax_ind character varying(1), 
    loc_tran_out_of_state_ind character varying(1), 
    loc_tran_dflt_taxgrp_nbr integer, 
    loc_tran_dflt_exmpt_taxgrp_nbr integer, 
    loc_tran_macrs_gozone_bonus_sw character varying(1), 
    loc_tran_start_sl_depr_sw character varying(1), 
    loc_tran_bc_seqnum integer, 
    loc_tran_ct_sysid character varying(6), 
    loc_tran_ct_override character varying(1), 
    loc_tran_ipaddr character varying(15), 
    loc_tran_migrate_date integer, 
    loc_tran_migrate_from character varying(10), 
    created_at timestamp without time zone, 
    updated_at timestamp without time zone, 
    row_creator_id bigint, 
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL 
); 
ALTER TABLE corrto.level2_locations OWNER TO postgres; 
CREATE SEQUENCE corrto.level2_locations_id_seq 
    START WITH 1 
    INCREMENT BY 1 
    NO MINVALUE 
    NO MAXVALUE 
    CACHE 1; 
ALTER TABLE corrto.level2_locations_id_seq OWNER TO postgres; 
ALTER SEQUENCE corrto.level2_locations_id_seq OWNED BY corrto.level2_locations.id; 
ALTER TABLE ONLY corrto.level2_locations ALTER COLUMN id SET DEFAULT nextval('corrto.level2_locations_id_seq'::regclass); 
ALTER TABLE ONLY corrto.level2_locations 
    ADD CONSTRAINT level2_locations_pkey PRIMARY KEY (id); 
CREATE UNIQUE INDEX index_level2_locations_on_loc_tran_loc ON corrto.level2_locations USING btree (loc_tran_loc); 
CREATE INDEX index_level2_locations_on_location_type_id ON corrto.level2_locations USING btree (location_type_id); 
CREATE INDEX index_level2_locations_on_row_creator_id ON corrto.level2_locations USING btree (row_creator_id); 
CREATE INDEX index_level2_locations_on_store_id ON corrto.level2_locations USING btree (store_id); 
