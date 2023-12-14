SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET search_path = cortst, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = false;
CREATE TABLE IF NOT EXISTS cortst.level2_refinfos
(
    id bigint NOT NULL,
    customer_id bigint,
    refinfo_seq_nbr integer,
    refinfo_company_name character varying(30) COLLATE pg_catalog."default",
    refinfo_last_name character varying(15) COLLATE pg_catalog."default",
    refinfo_first_name character varying(15) COLLATE pg_catalog."default",
    refinfo_address character varying(35) COLLATE pg_catalog."default",
    refinfo_city character varying(15) COLLATE pg_catalog."default",
    refinfo_state character varying(2) COLLATE pg_catalog."default",
    refinfo_zip_code integer,
    refinfo_phone bigint,
    refinfo_last_changed_date date,
    refinfo_map_coordinates character varying(7) COLLATE pg_catalog."default",
    refinfo_zip_4 integer,
    refinfo_ssan bigint,
    refinfo_drivers_license character varying(20) COLLATE pg_catalog."default",
    refinfo_relation character varying(35) COLLATE pg_catalog."default",
    refinfo_second_address_line character varying(35) COLLATE pg_catalog."default",
    refinfo_msg_nbr bigint,
    refinfo_company_switch character varying(1) COLLATE pg_catalog."default",
    refinfo_work_phone bigint,
    refinfo_cust_mailing_address character varying(25) COLLATE pg_catalog."default",
    refinfo_cust_mailing_state character varying(2) COLLATE pg_catalog."default",
    refinfo_cust_mailing_zip_4 integer,
    refinfo_cust_mailing_address_2 character varying(25) COLLATE pg_catalog."default",
    refinfo_zip_pc character varying(6) COLLATE pg_catalog."default",
    refinfo_work_phone_extension character varying(6) COLLATE pg_catalog."default",
    refinfo_cust_mailing_city character varying(15) COLLATE pg_catalog."default",
    refinfo_cust_mailing_zip_pc character varying(6) COLLATE pg_catalog."default",
    refinfo_marketing_opt_out character varying(1) COLLATE pg_catalog."default",
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    row_creator_id bigint,
    ht_etl_schema character varying(10) COLLATE pg_catalog."default" NOT NULL DEFAULT 'public'::character varying
);
ALTER TABLE cortst.level2_refinfos OWNER TO postgres;
CREATE SEQUENCE cortst.level2_refinfos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE cortst.level2_refinfos_id_seq OWNER TO postgres;
ALTER SEQUENCE cortst.level2_refinfos_id_seq OWNED BY cortst.level2_refinfos.id;
ALTER TABLE ONLY cortst.level2_refinfos ALTER COLUMN id SET DEFAULT nextval('level2_refinfos_id_seq'::regclass);
ALTER TABLE ONLY cortst.level2_refinfos
ADD CONSTRAINT level2_refinfos_pkey PRIMARY KEY (id);

