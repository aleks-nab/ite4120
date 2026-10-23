--liquibase formatted sql

--changeset aleks-nab:landuse-90-demo-data context:demo
--comment Demo rows for local development
/* Applied only under the demo context. The rule texts and legal references
   are illustrative examples for development and must be checked against the
   current legislation before anyone relies on them. */
insert into landuse.restriction_rule (restriction_type_code, forbidden, allowed_with_consent, consent_party, legal_basis, valid_from) values
    ('POWER_LINE_ZONE',
     'Erecting buildings, storing materials and planting tall trees inside the protection zone of the power line.',
     'Construction and earthworks inside the zone are possible only with the written consent of the network owner.',
     'Owner of the power line (network operator)',
     'Ehitusseadustik, ehitise kaitsevöönd; ehitise kaitsevööndi ulatuse määrus',
     '2026-01-01'),
    ('SHORE_BUILDING_EXCLUSION',
     'Building new structures inside the shore or bank building exclusion zone.',
     'Exceptions only where the law allows them, with the agreement of the Environmental Board.',
     'Keskkonnaamet (Environmental Board)',
     'Looduskaitseseadus § 38',
     '2026-01-01'),
    ('ROAD_PROTECTION_ZONE',
     'Building inside the road protection zone without agreement.',
     'Building is possible with the consent of the road owner.',
     'Road owner (Transport Administration or municipality)',
     'Ehitusseadustik, tee kaitsevöönd',
     '2026-01-01');

insert into landuse.pre_check (reference_number, client_request_id, cadastral_code, plot_area_m2, planned_building, contact_email, status, register_complete) values
    ('LU-2026-0001', 'demo-0001', '78401:101:0123', 1250.00, 'Single-family house, about 150 m2, with a garage.', 'owner@example.ee', 'NEW', true);

insert into landuse.pre_check_restriction (pre_check_id, restriction_type_code, restriction_name, restricting_object_owner, legal_basis, affected_area_m2, rule_id)
select pc.id, r.type_code, r.name, r.owner, r.basis, r.area::numeric, rr.id
from landuse.pre_check pc
join (values
    ('POWER_LINE_ZONE',      '10 kV elektriliini kaitsevöönd', 'Elektrilevi OÜ', 'Ehitusseadustik', '187.50'),
    ('ROAD_PROTECTION_ZONE', 'Kohaliku tee kaitsevöönd',       'Vallavalitsus',  'Ehitusseadustik', '96.00')
) as r(type_code, name, owner, basis, area) on true
left join landuse.restriction_rule rr on rr.restriction_type_code = r.type_code
where pc.reference_number = 'LU-2026-0001';
--
