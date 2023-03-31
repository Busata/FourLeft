create table message_log
(
    id                     UUID   not null primary key,
    message_type varchar(255),

    message_id             bigint not null,
    channel_id             bigint not null,

    content            text
);
