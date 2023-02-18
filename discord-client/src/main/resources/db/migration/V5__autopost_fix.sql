drop table auto_post_entry;
create table auto_post_entry
(
    id UUID not null primary key,

    event_id varchar(255) not null,
    challenge_id varchar(255) not null,
    message_id bigint not null,
    name         varchar(255)
)