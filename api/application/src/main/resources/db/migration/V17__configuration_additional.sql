alter table channel_configuration add column use_badges bool default false;

update channel_configuration set use_badges = true where club_id=418341;
update channel_configuration set use_badges = true where club_id=179084;
update channel_configuration set use_badges = true where club_id=431561;

alter table channel_configuration add column has_power_stage bool default false;

update channel_configuration set has_power_stage = true where club_id=431561;

alter table channel_configuration add column championship_points_type varchar(255) default 'DEFAULT';
update channel_configuration set championship_points_type = true where club_id=418341;
update channel_configuration set championship_points_type = true where club_id=431561;


alter table channel_configuration add column custom_championship_cycle int default 1;
update channel_configuration set custom_championship_cycle = 7 where club_id=418341;
update channel_configuration set custom_championship_cycle = 4 where club_id=431561;
