CREATE TABLE vendor_1099_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             INTEGER                                                        NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO vendor_1099_type_domain(id, value, description, localization_code)
VALUES (1, 1, 'Rents', 'rent'),
       (2, 2, 'Royalties', 'royalties'),
       (3, 3, 'Other Income', 'other'),
       (4, 4, 'Federal Income Tax Withheld', 'federal.income.tax.withheld'),
       (5, 5, 'Fishing Boat Proceeds', 'fishing.boat.proceeds'),
       (6, 6, 'Medical and Health Care Payments', 'medical.health.care.payments'),
       (7, 7, 'Payer made direct sales totaling $5,000 or more of consumer products to recipient for resale', 'payer.made.direct.sales.consumer.products'),
       (8, 8, 'Substitute payments in lieu of dividends or interest', 'substitute.payments'),
       (9, 9, 'Crop Insurance Proceeds', 'crop.insurance.proceeds'),
       (10, 10, 'Gross proceeds paid to an attorney', 'gross.proceeds.attorney'),
       (11, 11, 'Fish purchased for resale', 'fish.purchased.resale'),
       (12, 12, 'Section 409A Deferrals', 'section.409A.deferrals'),
       (13, 13, 'Excess Golden Parachute Payments', 'excess.golden.parachute'),
       (14, 14, 'Nonqualified Deferred Compensation', 'nonqualified.deferred.compensation'),
       (15, 15, 'State Tax Withheld', 'state.tax.withheld'),
       (16, 16, 'State/Payers State No', 'state.payers.state.no'),
       (17, 17, 'State Income', 'state.income'),
       (101, 101, 'Nonemployee Compensation', 'nonemployee.compensation'),
       (102, 102, 'Payer Made Direct Sales Totaling $5000 or more', 'payer.made.direct.sales'),
       (103, 103, 'Reserved for Future Use', 'reserved.future.use'),
       (104, 104, 'Federal Income Tax Withheld', 'federal.income.tax.withheld.box.104'),
       (105, 105, 'State Tax Withheld', 'state.tax.withheld.box.105'),
       (106, 106, 'State/Payers State No', 'state.payers.state.no.box.106'),
       (107, 107, 'State Income', 'state.income.box.107');
