alter table field_mapping add column context varchar default 'BACKEND';
update field_mapping set context='BACKEND';