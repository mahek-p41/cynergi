INSERT INTO security_access_point_type_domain (id, value, description, localization_code, area_id)
VALUES
   (172,'MCFSECGRPMNT','Security Group Maintenance','security.group.maintenance', null),
   (173,'MCFSECEMPGRPMNT','Security Employee in Group Maintenance','security.employee.in.group.maintenance', null),
   (174,'MCFSECGRPACCPTMNT','Security Group to Access Point Maintenance','security.group.to.access.point.maintenance', null),
   (175, 'POUTIL', 'Purchase Order Utilities', 'purchase.order.utilities', (SELECT id FROM area_type_domain WHERE value = 'PO'));
