--liquibase formatted sql

--changeset aleks-nab:landuse-schema
--comment Create the landuse schema
/* One schema per component: the land use restriction check (LANDUSE.01). */
create schema if not exists landuse;
--
