ALTER TABLE discord_club_configuration
    ADD auto_post_template VARCHAR;

ALTER TABLE discord_club_configuration
    ADD results_entry_template VARCHAR;
ALTER TABLE discord_club_configuration
    ADD guild_id BIGINT;

update discord_club_configuration set guild_id=892050958723469332;
update discord_club_configuration set results_entry_template='${badgeRank} **${rank}** • ${flag} • **${displayName}** • ${time} *(${deltaTime}*)';
update discord_club_configuration set auto_post_template='**Results** • ${eventCountryFlag} • **${lastStage}** • **${eventVehicleClass}** • *${totalEntries} entries*\n|entries:${badgeRank} • **${rank}** • ${flag} • **${displayName}** • ${platform} • ${totalTime} *${deltaTime}* • *${vehicle}*|'