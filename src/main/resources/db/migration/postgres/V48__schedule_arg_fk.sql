ALTER TABLE schedule_arg
   ADD CONSTRAINT schedule_arg_schedule_id_fk
   FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE;

CREATE INDEX schedule_arg_schedule_id_idx
   ON schedule_arg (schedule_id);
