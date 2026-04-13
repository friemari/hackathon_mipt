CREATE TABLE IF NOT EXISTS import_anomalies
(
    id bigserial NOT NULL,
    batch_id bigint,
    lead_id character varying(50) NOT NULL,
    field_name character varying(100) NOT NULL,
    expected_value character varying(255),
    actual_value character varying(255),
    message text,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT import_anomalies_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS import_batches
(
    id bigserial NOT NULL,
    file_name character varying(255) NOT NULL,
    started_at timestamp without time zone NOT NULL DEFAULT now(),
    finished_at timestamp without time zone,
    total_rows integer NOT NULL DEFAULT 0,
    inserted_rows integer NOT NULL DEFAULT 0,
    updated_rows integer NOT NULL DEFAULT 0,
    skipped_rows integer NOT NULL DEFAULT 0,
    error_rows integer NOT NULL DEFAULT 0,
    status character varying(20) NOT NULL DEFAULT 'RUNNING',
    CONSTRAINT import_batches_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS import_errors
(
    id bigserial NOT NULL,
    batch_id bigint,
    row_number integer NOT NULL,
    lead_id character varying(50),
    error_message text NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT import_errors_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS lead_groups
(
    id character varying(50) NOT NULL,
    name character varying(255) NOT NULL,
    CONSTRAINT lead_groups_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS leads
(
    id character varying(50) NOT NULL,
    group_id character varying(50),
    b2c_manager_id character varying(50),
    delivery_manager_id character varying(50),
    qualification character varying(50),
    delivery_service character varying(255),
    city character varying(255),
    sale_date date NOT NULL,
    created_at timestamp without time zone,
    sale_ts timestamp without time zone,
    to_assembly_ts timestamp without time zone,
    handed_to_delivery_ts timestamp without time zone,
    handed_to_delivery_alt_ts timestamp without time zone,
    issued_or_pvz_ts timestamp without time zone,
    received_ts timestamp without time zone,
    rejected_ts timestamp without time zone,
    returned_ts timestamp without time zone,
    closed_ts timestamp without time zone,
    lifecycle_incomplete boolean DEFAULT false,
    outcome_unknown boolean DEFAULT false,
    buyout_flag boolean DEFAULT false,
    CONSTRAINT leads_pkey PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS import_anomalies
    ADD CONSTRAINT import_anomalies_batch_id_fkey FOREIGN KEY (batch_id)
    REFERENCES public.import_batches (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

ALTER TABLE IF EXISTS import_errors
    ADD CONSTRAINT import_errors_batch_id_fkey FOREIGN KEY (batch_id)
    REFERENCES public.import_batches (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

ALTER TABLE IF EXISTS leads
    ADD CONSTRAINT leads_group_id_fkey FOREIGN KEY (group_id)
    REFERENCES public.lead_groups (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;