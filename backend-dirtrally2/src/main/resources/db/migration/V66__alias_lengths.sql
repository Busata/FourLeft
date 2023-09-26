ALTER TABLE alias_update_request ALTER COLUMN discord_id type VARCHAR;
ALTER TABLE alias_update_request ALTER COLUMN requested_alias type VARCHAR;
ALTER TABLE player_info_aliases ALTER COLUMN aliases type VARCHAR;
ALTER TABLE alias_update_log ALTER COLUMN discord_id type VARCHAR;
ALTER TABLE alias_update_log ALTER COLUMN changes type VARCHAR;