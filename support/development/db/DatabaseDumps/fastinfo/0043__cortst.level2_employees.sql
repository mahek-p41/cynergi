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
CREATE TABLE cortst.level2_employees (
    id bigint NOT NULL,
    emp_nbr bigint,
    emp_last_name character varying(15),
    emp_first_name_mi character varying(15),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    row_creator_id bigint,
    employee_store_id bigint,
    emp_address character varying(25),
    emp_city character varying(15),
    emp_state character varying(2),
    emp_zip_code integer,
    emp_home_phone bigint,
    emp_work_phone bigint,
    emp_ssan bigint,
    emp_store_nbr integer,
    emp_drivers_license character varying(20),
    emp_pass_1 character varying(1),
    emp_marital_status character varying(1),
    emp_spouse_name character varying(25),
    emp_pass_2 character varying(1),
    emp_wage_type character varying(1),
    emp_wage_amt numeric(11,2),
    emp_gl_nbr_nbr integer,
    emp_gl_nbr_profit_center integer,
    emp_acct_mgr_indr character varying(1),
    emp_commission_indr numeric(1,0),
    emp_commission_percent numeric(11,1),
    emp_override_indr character varying(1),
    emp_override_percent numeric(11,1),
    emp_in_out_indr character varying(1),
    emp_first_employment_date date,
    emp_uniform_indr character varying(1),
    emp_uniform_amt numeric(11,2),
    emp_insurance_indr character varying(1),
    emp_insurance_amt numeric(11,2),
    emp_car_allowance_indr character varying(1),
    emp_car_allowance_amt numeric(11,2),
    emp_accts_recv_indr character varying(1),
    emp_accts_recv_amt numeric(11,2),
    emp_ira_indr character varying(1),
    emp_ira_amt numeric(11,2),
    emp_hrs_worked integer,
    emp_min_worked integer,
    emp_pass_3 character varying(1),
    emp_ot_pass_1 character varying(1),
    emp_pass_4 character varying(1),
    emp_ot_authority_indr character varying(1),
    emp_ot_pass_2 character varying(1),
    emp_pass_5 character varying(1),
    emp_ot_pass_3 character varying(1),
    emp_time_hrs integer,
    emp_time_mins integer,
    emp_time_secs integer,
    emp_last_review_date date,
    emp_review_rating character varying(1),
    emp_last_raise_date date,
    emp_last_raise_amt numeric(11,2),
    emp_other_id_nbr character varying(4),
    emp_termination_date date,
    emp_pass_6 character varying(1),
    emp_ot_pass_4 character varying(1),
    emp_ot_pass_5 character varying(1),
    emp_ot_pass_6 character varying(1),
    emp_over_8_hrs_indr character varying(1),
    emp_ok_past_midnite character varying(1),
    emp_over_eight_hrs integer,
    emp_over_eight_min integer,
    emp_date_in date,
    emp_work_comp_code character varying(4),
    emp_pay_freq character varying(1),
    emp_vac_hrs numeric(11,2),
    emp_sick_hrs numeric(11,2),
    emp_vac_add_hrs numeric(11,2),
    emp_sick_add_hrs numeric(11,2),
    emp_holiday_hrs numeric(11,2),
    emp_jury_hrs numeric(11,2),
    emp_bereavement_hrs numeric(11,2),
    emp_comp_hrs numeric(11,2),
    emp_comp_prev_hrs numeric(11,2),
    emp_present_loc integer,
    emp_require_loc_entry character varying(1),
    emp_over_8_date date,
    emp_day_total_hrs integer,
    emp_day_total_min integer,
    emp_ot_break_hrs integer,
    emp_tech_indr character varying(1),
    emp_last_changed_date date,
    emp_supervisor character varying(1),
    emp_auto_lunch_indr character varying(1),
    emp_min_hrs_worked numeric(11,2),
    emp_lunch_hrs_to_deduct numeric(11,2),
    emp_pager_number bigint,
    emp_pager_pin bigint,
    emp_mobil_number bigint,
    emp_check_hrs_in integer,
    emp_check_min_in integer,
    emp_check_hrs_out integer,
    emp_check_min_out integer,
    emp_military_hrs numeric(11,2),
    emp_holiday_add_hrs numeric(11,2),
    emp_jury_add_hrs numeric(11,2),
    emp_bereavement_add_hrs numeric(11,2),
    emp_military_add_hrs numeric(11,2),
    emp_base_commission_indr character varying(1),
    emp_birth_date date,
    emp_zip_pc character varying(6),
    emp_country_code character varying(3),
    emp_date_termination_date_chgd date,
    emp_date_password_changed date,
    emp_alt_store_indr character varying(1),
    emp_alt_area integer,
    emp_alt_synonyms character varying(1),
    emp_eom_indr character varying(1),
    emp_oth_indr character varying(1),
    emp_dflt_term_rec integer,
    ht_etl_schema character varying(10) DEFAULT 'public'::character varying NOT NULL,
    department_id bigint
);
ALTER TABLE cortst.level2_employees OWNER TO fastinfo_dba;
CREATE SEQUENCE cortst.level2_employees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE cortst.level2_employees_id_seq OWNER TO fastinfo_dba;
ALTER SEQUENCE cortst.level2_employees_id_seq OWNED BY cortst.level2_employees.id;
ALTER TABLE ONLY cortst.level2_employees ALTER COLUMN id SET DEFAULT nextval('cortst.level2_employees_id_seq'::regclass);
ALTER TABLE ONLY cortst.level2_employees
    ADD CONSTRAINT level2_employees_pkey PRIMARY KEY (id);
CREATE INDEX index_level2_employees_on_department_id ON cortst.level2_employees USING btree (department_id);
CREATE INDEX index_level2_employees_on_emp_first_name_mi ON cortst.level2_employees USING btree (emp_first_name_mi);
CREATE INDEX index_level2_employees_on_emp_last_name ON cortst.level2_employees USING btree (emp_last_name);
CREATE UNIQUE INDEX index_level2_employees_on_emp_nbr ON cortst.level2_employees USING btree (emp_nbr);
CREATE INDEX index_level2_employees_on_employee_store_id ON cortst.level2_employees USING btree (employee_store_id);
CREATE INDEX index_level2_employees_on_row_creator_id ON cortst.level2_employees USING btree (row_creator_id);
