create table auto_post_entry
(
    id UUID not null primary key,

    event_id varchar(255) not null,
    challenge_id varchar(255) not null,
    message_id bigint not null,
    rank         bigint  not null,
    name         varchar(255),
    nationality  varchar(255),
    vehicle varchar(255),

    total_time   varchar(255),
    total_diff   varchar(255),
    last_stage_time   varchar(255),
    last_stage_diff   varchar(255),
    is_dnf       boolean not null
)