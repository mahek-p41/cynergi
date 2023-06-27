DROP TRIGGER IF EXISTS update_security_group_trg ON security_group;

CREATE TRIGGER update_security_group_trg
    BEFORE UPDATE
    ON security_group
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();