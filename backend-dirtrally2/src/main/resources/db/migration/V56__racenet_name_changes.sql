ALTER TABLE player_info
    ADD created_before_racenet_change BOOLEAN default false;

ALTER TABLE player_info
    ALTER COLUMN created_before_racenet_change SET NOT NULL;

update player_info set created_before_racenet_change=true;
