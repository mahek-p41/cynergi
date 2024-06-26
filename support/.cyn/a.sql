--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.24
-- Dumped by pg_dump version 14.11 (Homebrew)

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

--
-- Name: users; Type: TABLE; Schema: public; Owner: fastinfo_dba
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255) DEFAULT ''::character varying NOT NULL,
    encrypted_password character varying(255) DEFAULT ''::character varying NOT NULL,
    reset_password_token character varying(255),
    reset_password_sent_at timestamp without time zone,
    remember_created_at timestamp without time zone,
    sign_in_count integer DEFAULT 0,
    current_sign_in_at timestamp without time zone,
    last_sign_in_at timestamp without time zone,
    current_sign_in_ip character varying(255),
    last_sign_in_ip character varying(255),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    business_id bigint NOT NULL
);


ALTER TABLE public.users OWNER TO fastinfo_dba;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: fastinfo_dba
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO fastinfo_dba;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fastinfo_dba
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: fastinfo_dba
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: fastinfo_dba
--

COPY public.users (id, email, encrypted_password, reset_password_token, reset_password_sent_at, remember_created_at, sign_in_count, current_sign_in_at, last_sign_in_at, current_sign_in_ip, last_sign_in_ip, created_at, updated_at, business_id) FROM stdin;
6	998@corrto.local	$2a$10$w3VJo93S3Y3ZVrVGX/aaxOTbm560O6f0z0nM.FUtudziPH3Oigrgq	\N	\N	\N	37	2024-03-22 16:57:13.016695	2024-03-22 16:56:27.966123	172.25.3.10	172.25.3.10	2020-05-07 13:17:05.597282	2024-03-22 16:57:13.017317	8
8	998@corron.local	$2a$10$D/nn5RiPBF9pdf/hEOU8qeguCUMGSZVPLbXnzO8XGhDrE0CkSk7vK	\N	\N	\N	7	2023-03-06 08:49:55.266019	2021-10-25 14:27:07.635278	172.25.3.9	172.19.60.42	2021-10-15 16:18:02.11038	2023-03-06 08:49:55.26695	10
7	998@coravt.local	$2a$10$0CHhl1sRmXsvW4eg6ZWIzujjp5MU5BB82lhbX4T2K7Qkk8qJCGwjK	\N	\N	\N	5	2023-03-06 08:50:30.143078	2021-11-19 13:26:59.25929	172.25.3.9	172.25.3.3	2021-10-15 16:16:36.321848	2023-03-06 08:50:30.1472	9
10	98@corron.local		\N	\N	\N	0	\N	\N	\N	\N	\N	\N	9
\.


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fastinfo_dba
--

SELECT pg_catalog.setval('public.users_id_seq', 10, true);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: fastinfo_dba
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: index_users_on_business_id; Type: INDEX; Schema: public; Owner: fastinfo_dba
--

CREATE INDEX index_users_on_business_id ON public.users USING btree (business_id);


--
-- Name: index_users_on_email; Type: INDEX; Schema: public; Owner: fastinfo_dba
--

CREATE UNIQUE INDEX index_users_on_email ON public.users USING btree (email);


--
-- Name: index_users_on_reset_password_token; Type: INDEX; Schema: public; Owner: fastinfo_dba
--

CREATE UNIQUE INDEX index_users_on_reset_password_token ON public.users USING btree (reset_password_token);


--
-- PostgreSQL database dump complete
--

