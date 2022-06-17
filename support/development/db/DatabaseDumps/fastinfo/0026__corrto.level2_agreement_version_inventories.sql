SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET search_path = corrto, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = false;
CREATE TABLE level2_agreement_version_inventories (
    id bigint NOT NULL,
    agreement_version_id bigint,
    inventory_id bigint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    row_creator_id bigint,
    etl_time date,
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL
);
ALTER TABLE corrto.level2_agreement_version_inventories OWNER TO postgres;
CREATE SEQUENCE level2_agreement_version_inventories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE corrto.level2_agreement_version_inventories_id_seq OWNER TO postgres;
ALTER SEQUENCE level2_agreement_version_inventories_id_seq OWNED BY level2_agreement_version_inventories.id;
ALTER TABLE ONLY level2_agreement_version_inventories ALTER COLUMN id SET DEFAULT nextval('level2_agreement_version_inventories_id_seq'::regclass);
ALTER TABLE ONLY level2_agreement_version_inventories
    ADD CONSTRAINT level2_agreement_version_inventories_pkey PRIMARY KEY (id);
CREATE INDEX index_level2_agreement_version_inventories_on_agreement_version_id ON level2_agreement_version_inventories USING btree (agreement_version_id);
CREATE INDEX index_level2_agreement_version_inventories_on_inventory_id ON level2_agreement_version_inventories USING btree (inventory_id);
CREATE INDEX index_level2_agreement_version_inventories_on_row_creator_id ON level2_agreement_version_inventories USING btree (row_creator_id);

