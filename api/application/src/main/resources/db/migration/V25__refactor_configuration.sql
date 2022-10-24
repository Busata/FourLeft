CREATE TABLE club_configuration
(
    id                        UUID NOT NULL,
    description               VARCHAR,
    club_id                   BIGINT,
    automated_generation_type VARCHAR(255),
    CONSTRAINT pk_club_configuration PRIMARY KEY (id)
);

insert into club_configuration values (uuid_generate_v4(), 'MaintMaster SRD Daily', 432100, 'DAILY');
insert into club_configuration values (uuid_generate_v4(), 'Dirty Dailies', 418341, 'DAILY');
insert into club_configuration values (uuid_generate_v4(), 'Dirty Monthlies', 431561, 'MONTHLY');


alter table channel_configuration drop column automated_generation_type;