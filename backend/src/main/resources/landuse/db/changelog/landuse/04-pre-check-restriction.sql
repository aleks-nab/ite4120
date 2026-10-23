--liquibase formatted sql

--changeset aleks-nab:landuse-04-pre-check-restriction
--comment Restrictions found on the plot at the time of a pre-check
/* A snapshot, not a copy of the register: it records what the owner was
   shown on that day, including the rule that explained it. rule_id is empty
   when no rule existed for that type yet. */
create table landuse.pre_check_restriction (
    id                  bigint default nextval('core.seq_id') not null,
    pre_check_id        bigint not null,
    restriction_type_code text not null,
    restriction_name    text not null,
    restricting_object_owner text,
    legal_basis         text,
    affected_area_m2    numeric(12,2),
    rule_id             bigint,
    sys_status          char(1),
    sys_version         int,
    sys_created_at      timestamptz,
    sys_created_by      text,
    sys_modified_at     timestamptz,
    sys_modified_by     text,
    constraint pre_check_restriction_pk primary key (id),
    constraint pre_check_restriction_pre_check_fk foreign key (pre_check_id) references landuse.pre_check (id),
    constraint pre_check_restriction_type_fk foreign key (restriction_type_code) references landuse.restriction_type (code),
    constraint pre_check_restriction_rule_fk foreign key (rule_id) references landuse.restriction_rule (id),
    constraint pre_check_restriction_area_chk check (affected_area_m2 is null or affected_area_m2 >= 0)
);

select core.create_table_metadata('landuse.pre_check_restriction');

/* Every foreign key indexed (TEHIK non-functional requirement 6.4). */
create index pre_check_restriction_pre_check_idx on landuse.pre_check_restriction (pre_check_id) where (sys_status = 'A');
create index pre_check_restriction_type_idx on landuse.pre_check_restriction (restriction_type_code) where (sys_status = 'A');
create index pre_check_restriction_rule_idx on landuse.pre_check_restriction (rule_id) where (sys_status = 'A');
--
