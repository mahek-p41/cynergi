INSERT INTO security_access_point_type_domain (id, value, description, localization_code, area_id)
VALUES
   (171,'GLSUMGLINTMV','General Ledger Special Move Staging','general.ledger.special.move.staging', (SELECT id FROM area_type_domain WHERE value = 'GL'));
