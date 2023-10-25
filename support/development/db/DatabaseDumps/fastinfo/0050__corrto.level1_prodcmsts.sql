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
    updated_at timestamp without time zone
);
ALTER TABLE corrto.level1_prodcmsts OWNER TO fastinfo_dba;
