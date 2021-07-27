CREATE TABLE event_schedule (
  id               UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
  time_created     TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
  time_updated     TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
  company_id       BIGINT                                                   NOT NULL,
  job_name         VARCHAR(30) CHECK (job_name in ('Payment'))              NOT NULL,
  job_description  VARCHAR(100)                                             NOT NULL,
  only_run_once    BOOLEAN     DEFAULT false                                NOT NULL,
  frequency        VARCHAR(1)  CHECK (frequency in ('D','B','W','M','Z'))   NOT NULL,
  weekly_run_days  VARCHAR(27),
  monthly_run_days VARCHAR(10),
  start_date       date                                                     NOT NULL,
  start_time       time        with time zone,
  end_date         date,
  end_time         time        with time zone,
  last_run_date    TIMESTAMPTZ,
  job_enabled      BOOLEAN     DEFAULT true                                 NOT NULL,
  UNIQUE (company_id, job_name, job_description)
);
-- only_run_once    --> After this job runs, set job_enabled to false
-- frequency        --> 'Z' means last day of the month
-- weekly_run_days  --> Example: "Sun#Fri#Sat"
-- monthly_run_days --> Example: "5#20" -- the actual day(s) to execute
-- monthly_run_days --> Example: "1stSun" or "4thSat" -- First Sunday/Fourth Saturday of the month, etc


CREATE OR REPLACE FUNCTION update_user_table_fn()
    RETURNS TRIGGER AS
$$
BEGIN
    IF new.id <> old.id THEN -- help ensure that the id can't be updated once it is created
        RAISE EXCEPTION 'cannot update id once it has been created';
    END IF;
    new.time_updated := clock_timestamp();
    RETURN new;
END;
$$
    LANGUAGE plpgsql;

-- Begin check for weekly_run_days
CREATE FUNCTION event_schedule_weekly_guard_fn()
  RETURNS TRIGGER AS
$$
BEGIN
  IF new.frequency = 'W' and new.weekly_run_days is null
  THEN
     RAISE EXCEPTION 'Weekly needs which days to run';
  END IF;
  RETURN new;
END;
$$
    LANGUAGE plpgsql;

-- Begin check for weekly_run_days for BI-WEEKLY
CREATE FUNCTION event_schedule_biweekly_guard_fn()
  RETURNS TRIGGER AS
$$
BEGIN
  IF new.frequency = 'B' and new.weekly_run_days is null
  THEN
     RAISE EXCEPTION 'Bi-weekly needs which days to run';
  END IF;
  RETURN new;
END;
$$
    LANGUAGE plpgsql;

-- Begin check for monthly_run_days
CREATE FUNCTION event_schedule_monthly_guard_fn()
  RETURNS TRIGGER AS
$$
BEGIN
  IF new.frequency = 'M' and new.monthly_run_days is null
  THEN
     RAISE EXCEPTION 'Monthly needs the day number to run';
  END IF;
  RETURN new;
END;
$$
    LANGUAGE plpgsql;

CREATE TRIGGER upsert_event_schedule_weekly_guard_trg
   BEFORE UPDATE or INSERT
   ON event_schedule
   FOR EACH ROW
   EXECUTE PROCEDURE event_schedule_weekly_guard_fn();

CREATE TRIGGER upsert_event_schedule_biweekly_guard_trg
   BEFORE UPDATE or INSERT
   ON event_schedule
   FOR EACH ROW
   EXECUTE PROCEDURE event_schedule_biweekly_guard_fn();

CREATE TRIGGER upsert_event_schedule_monthly_guard_trg
   BEFORE UPDATE or INSERT
   ON event_schedule
   FOR EACH ROW
   EXECUTE PROCEDURE event_schedule_monthly_guard_fn();



