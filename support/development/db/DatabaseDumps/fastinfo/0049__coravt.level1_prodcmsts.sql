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
CREATE TABLE coravt.level1_prodcmsts (
    prodcmst_class_code character varying(1),
    prodcmst_class_description character varying(15),
    prodcmst_straight_line_book_depreciation_start_date date,
    prodcmst_straight_line_book_depreciation_end_date date,
    prodcmst_income_forecasting_book_depreciation_start_date date,
    prodcmst_income_forecasting_book_depreciation_end_date date,
    prodcmst_macrs_book_start_date date,
    prodcmst_macrs_book_end_date date,
    prodcmst_macrs_tax_start_date date,
    prodcmst_macrs_tax_end_date date,
    prodcmst_rent_switch character varying(1),
    prodcmst_transition_into_switch character varying(1),
    prodcmst_transition_out_of_switch character varying(1),
    prodcmst_cash_sale_switch character varying(1),
    prodcmst_allow_depreciation_switch character varying(1),
    prodcmst_allow_straight_line_life_switch character varying(1),
    time_created timestamp without time zone,
    time_updated timestamp without time zone
);
ALTER TABLE coravt.level1_prodcmsts OWNER TO fastinfo_dba;
