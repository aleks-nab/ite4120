--liquibase formatted sql

--changeset aleks-nab:landuse-02-restriction-rule
--comment Rule catalogue: what a restriction type means for building
/* A business table: id from core.seq_id, sys columns filled by the trigger.
   A type can have successive rules over time, so each rule carries a
   validity period, and a saved pre-check can point at the exact rule it
   showed even after that rule is retired. */
create table landuse.restriction_rule (
    id                  bigint default nextval('core.seq_id') not null,
    restriction_type_code text not null,
    forbidden           text not null,
    allowed_with_consent text,
    consent_party       text,
    legal_basis         text not null,
    valid_from          date not null,
    valid_until         date,
    sys_status          char(1),
    sys_version         int,
    sys_created_at      timestamptz,
    sys_created_by      text,
    sys_modified_at     timestamptz,
    sys_modified_by     text,
    constraint restriction_rule_pk primary key (id),
    constraint restriction_rule_type_fk foreign key (restriction_type_code) references landuse.restriction_type (code),
    constraint restriction_rule_validity_chk check (valid_until is null or valid_until >= valid_from)
);

select core.create_table_metadata('landuse.restriction_rule');

create index restriction_rule_type_idx on landuse.restriction_rule (restriction_type_code) where (sys_status = 'A');
--
