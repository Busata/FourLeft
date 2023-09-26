ALTER TABLE wrcticker_entry
    RENAME TO fiaticker_entry;

alter table fiaticker_entry add column source varchar;

update fiaticker_entry set source='WRC';
