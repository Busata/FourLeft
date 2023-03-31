CREATE TABLE discord_channel_club_view_configurations
(
    commands_club_view_configuration_id UUID NOT NULL,
    discord_channel_configuration_id    UUID NOT NULL
);

ALTER TABLE discord_channel_club_view_configurations
    ADD CONSTRAINT fk_dischacluviecon_on_club_view FOREIGN KEY (commands_club_view_configuration_id) REFERENCES club_view (id);

ALTER TABLE discord_channel_club_view_configurations
    ADD CONSTRAINT fk_dischacluviecon_on_discord_channel_configuration FOREIGN KEY (discord_channel_configuration_id) REFERENCES discord_channel_configuration (id);

insert into discord_channel_club_view_configurations (discord_channel_configuration_id, commands_club_view_configuration_id)
select id, club_view_configuration_id from discord_channel_configuration;