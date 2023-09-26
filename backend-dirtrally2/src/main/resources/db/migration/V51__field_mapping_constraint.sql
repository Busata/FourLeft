delete from field_mapping where context='FRONTEND';
alter table field_mapping add constraint unique_mappings unique (name, type, context);
