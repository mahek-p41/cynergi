ALTER TRIGGER update_aws_token_trg ON aws_token RENAME TO update_sign_here_token_trg;

ALTER INDEX aws_token_store_number_sfk_idx RENAME TO sign_here_token_store_number_sfk_idx;

ALTER TABLE aws_token RENAME TO sign_here_token;


