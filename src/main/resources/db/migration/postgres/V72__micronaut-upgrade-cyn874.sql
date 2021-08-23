CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- create wrapper function for hashing passwords
CREATE OR REPLACE FUNCTION hash_passcode(TEXT)
   RETURNS TEXT AS
$$
BEGIN
   IF $1 IS NOT NULL AND length($1) > 2 THEN
      RETURN crypt($1, gen_salt('bf', 10));
   ELSE
      RAISE EXCEPTION 'Pass code provided does not meet length requirement of 3';
   END IF;
END;
$$ LANGUAGE plpgsql;
