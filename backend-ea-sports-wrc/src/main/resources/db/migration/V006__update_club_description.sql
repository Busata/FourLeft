alter table club alter column club_description type varchar using club_description::varchar;

update club_configuration set keep_synced=true where 1=1;
