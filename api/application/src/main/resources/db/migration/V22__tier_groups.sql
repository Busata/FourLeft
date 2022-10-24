CREATE TABLE tier_group
(
    id   UUID NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_tier_group PRIMARY KEY (id)
);

ALTER TABLE tier
    ADD tier_group_id UUID;

ALTER TABLE tier
    ADD CONSTRAINT FK_TIER_ON_TIER_GROUP FOREIGN KEY (tier_group_id) REFERENCES tier_group (id);


insert into tier_group values (uuid_generate_v4(), 'GRF Special Events');

update tier set tier_group_id=(select id from tier_group limit 1);