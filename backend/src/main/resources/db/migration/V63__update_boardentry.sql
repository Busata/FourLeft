create index idx_racenets on player_info_racenets(racenets);
update board_entry be set player_info_id=(select pir.player_info_id from player_info_racenets pir where be.name = pir.racenets);