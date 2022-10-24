create table club
(
    id           uuid   not null
        primary key,
    description  varchar(255),
    last_update  timestamp,
    members      bigint not null,
    name         varchar(255),
    reference_id bigint
);

create table championship
(
    id                   uuid    not null
        primary key,
    allow_assists        boolean not null,
    force_cockpit_camera boolean not null,
    hardcore_damage      boolean not null,
    is_active            boolean not null,
    name                 varchar(255),
    reference_id         varchar(255),
    unexpected_moments   boolean not null,
    club_id              uuid
        constraint fkpev84vpt7g9302x910ot2v96n
            references club
);

create table community_challenge
(
    id            uuid    not null
        primary key,
    challenge_id  varchar(255),
    end_time      timestamp,
    ended         boolean not null,
    isdlc         boolean not null,
    start_time    timestamp,
    synced        boolean not null,
    type          varchar(255),
    vehicle_class varchar(255)
);

create table community_event
(
    id           uuid not null
        primary key,
    discipline   varchar(255),
    event_id     varchar(255),
    name         varchar(255),
    challenge_id uuid
        constraint fklr6fk6qiee012tnj78u2mo7um
            references community_challenge
);

create table community_leaderboard_tracking
(
    id                uuid    not null
        primary key,
    alias             varchar(255),
    nick_name         varchar(255),
    track_daily       boolean not null,
    track_monthly     boolean not null,
    track_rally_cross boolean not null,
    track_weekly      boolean not null
);

create table community_stage
(
    id       uuid not null
        primary key,
    country  varchar(255),
    location varchar(255),
    name     varchar(255),
    stage_id varchar(255),
    event_id uuid
        constraint fklbhw03of6pnm8fyywhcfmd8cu
            references community_event
);

create table event
(
    id                       uuid not null
        primary key,
    challenge_id             varchar(255),
    country                  varchar(255),
    event_status             varchar(255),
    first_stage_condition    varchar(255),
    last_result_checked_time timestamp,
    name                     varchar(255),
    reference_id             varchar(255),
    vehicle_class            varchar(255),
    championship_id          uuid
        constraint fka3kqunr1341dpppf5hmwi8nf1
            references championship
);

create table leaderboard
(
    id           uuid not null
        primary key,
    challenge_id varchar(255),
    event_id     varchar(255),
    stage_id     varchar(255)
);

create table board_entry
(
    id             uuid    not null
        primary key,
    is_dnf         boolean not null,
    name           varchar(255),
    nationality    varchar(255),
    rank           bigint  not null,
    stage_diff     varchar(255),
    stage_time     varchar(255),
    total_diff     varchar(255),
    total_time     varchar(255),
    vehicle_name   varchar(255),
    leaderboard_id uuid
        constraint fklbn0t975ml1hqyksainqjuh1a
            references leaderboard
);

create table stage
(
    id           uuid not null
        primary key,
    name         varchar(255),
    reference_id varchar(255),
    event_id     uuid
        constraint fkjmbqqal4hky5pmlrq8k1cmntp
            references event
);

create table standing_entry
(
    id              uuid not null
        primary key,
    display_name    varchar(255),
    nationality     varchar(255),
    rank            bigint,
    total_points    bigint,
    championship_id uuid
        constraint fk4ixnhx46ukhvoyrrto2x6qibq
            references championship
);
