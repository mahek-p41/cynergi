CREATE TABLE inventory_end_of_month_inventory_indr_type_domain
(
      id                INTEGER                                                              NOT NULL PRIMARY KEY,
      value             VARCHAR(100)                                                         NOT NULL,
      description       VARCHAR(100)   CHECK ( char_length(trim(description)) > 1)           NOT NULL,
      localization_code VARCHAR(100)   CHECK ( char_length(trim(localization_code)) > 1)     NOT NULL,
      UNIQUE(value)
);

COMMENT ON TABLE inventory_end_of_month_inventory_indr_type_domain IS 'Valid values for inventory’s class.';

INSERT INTO inventory_end_of_month_inventory_indr_type_domain(id, value, description, localization_code)
VALUES (1, 'F', 'Fixed Asset', 'fixed.asset'),
       (2, 'O', 'Rent-To-Own', 'rent.to.own'),
       (3, 'R', 'Rent-To-Rent', 'rent.to.rent'),
       (4, 'S', 'Sales/Retail', 'sales.retail');
