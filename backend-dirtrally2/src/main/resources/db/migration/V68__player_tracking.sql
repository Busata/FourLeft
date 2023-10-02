ALTER TABLE player_info
    ADD track_community BOOLEAN;

update player_info set track_community=false where 1=1;


update player_info set track_community=true where player_info.racenet in (select community_leaderboard_tracking.racenet from community_leaderboard_tracking);
update player_info set track_community=true where player_info.racenet in (select community_leaderboard_tracking.alias from community_leaderboard_tracking);
update player_info set track_community=true where id in (select player_info_id from player_info_aliases where aliases in (select racenet from community_leaderboard_tracking));
update player_info set track_community=true where id in (select player_info_id from player_info_aliases where aliases in (select alias from community_leaderboard_tracking));