CREATE TABLE ship_via (
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    description varchar(30) CHECK ( char_length(trim(description)) > 1)
  );
CREATE TRIGGER update_shipvia_trg
   BEFORE UPDATE
   ON ship_via
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
