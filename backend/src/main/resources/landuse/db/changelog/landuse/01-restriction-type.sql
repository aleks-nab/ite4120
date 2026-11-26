--liquibase formatted sql

--changeset aleks-nab:landuse-01-restriction-type
--comment Restriction type reference table
/* Reference list of restriction types. Natural key, no sys columns: the same
   shape as animals.species. Both the rule catalogue and the restrictions found
   on a plot point at this list. */
create table landuse.restriction_type (
    code                text not null,
    name                text not null,
    constraint restriction_type_pk primary key (code)
);
--

--changeset aleks-nab:landuse-01-restriction-type-seed
--comment Seed the restriction type list
/* Reference data in its own changeset, convergent: ON CONFLICT DO NOTHING
   lets it land safely on a database that already holds some of the rows. */
insert into landuse.restriction_type (code, name) values
    ('POWER_LINE_ZONE',          'Elektriliini kaitsevöönd'),
    ('GAS_PIPELINE_ZONE',        'Gaasitorustiku kaitsevöönd'),
    ('ROAD_PROTECTION_ZONE',     'Tee kaitsevöönd'),
    ('SHORE_BUILDING_EXCLUSION', 'Ranna või kalda ehituskeeluvöönd'),
    ('HERITAGE_PROTECTION',      'Kultuurimälestise kaitsevöönd'),
    ('NATURE_PROTECTION',        'Looduskaitseline piirang')
on conflict do nothing;
--
