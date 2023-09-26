alter table player_info add column platform_name varchar not null default '';
update player_info set platform_name='';
