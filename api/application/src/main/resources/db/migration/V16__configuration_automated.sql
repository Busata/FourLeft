alter table channel_configuration add column automated_generation_type varchar(255) default 'NO_GENERATION';

update channel_configuration set automated_generation_type = 'DAILY' where club_id=418341;
update channel_configuration set automated_generation_type = 'DAILY' where club_id=432100;
update channel_configuration set automated_generation_type = 'MONTHLY' where club_id=431561;