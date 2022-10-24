create table club_member
(
    id                   uuid    not null
        primary key,
    reference_id         varchar(255),

    display_name varchar(255),
    membership_type varchar(255),
    championship_golds bigint,
    championship_silvers bigint,
    championship_bronzes bigint,
    championship_participation bigint,
    club_id              uuid not null,
    foreign key (club_id) references club (id)
);
