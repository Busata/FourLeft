create table channel_configuration
(
    id                     UUID   not null primary key,
    description            text,
    channel_id             bigint not null,
    club_id                bigint,
    post_club_results      bool,
    post_community_results bool
);

insert into channel_configuration
values (uuid_generate_v4(), 'GRF Daily', 972203349107683388, 417474, true, false);

insert into channel_configuration
values (uuid_generate_v4(), 'GRF Special events', 831903667409125377, 380718, true, false);

insert into channel_configuration
values (uuid_generate_v4(), 'GRF Championship', 817405818349682729, 377197, true, false);

insert into channel_configuration
values (uuid_generate_v4(), 'Dirty Weeklies', 892373522473685054, 179084, true, false);

insert into channel_configuration
values (uuid_generate_v4(), 'Dirt Rally 2 (Main chat)', 892369709780070410, null, false, true);