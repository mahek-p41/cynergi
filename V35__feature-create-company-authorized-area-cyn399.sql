CREATE TABLE company_to_area_type (
	 company_id BIGINT REFERENCES company(id)                                    NOT NULL,
	 area_type_id  BIGINT REFERENCES area_type_domain(id)                        NOT NULL,
	 UNIQUE(company_id,area_type_id)
 );

COMMENT ON TABLE company_to_area_type IS 'contains the id associated with the individual modules/systems that the company has agreed to implement. i.e. PO, AP, GL';
