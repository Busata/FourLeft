drop view if exists community_daily_entries;
create view community_daily_entries
as
select uuid_generate_v4() as id,
        be.name as name,
       be.total_time as total_time,
       be.total_diff as total_diff,
       be.rank as rank,
       cs.country as country,
       cc.end_time as end_time,
       cs.name as stage_name,
       cc.vehicle_class as vehicle_class,
       be.vehicle_name as vehicle,
       be.is_dnf as is_dnf,
       l.id as leaderboard_id,
       ranks.total as total_rank
from community_challenge cc
         join community_event ce on cc.id = ce.challenge_id
         join community_stage cs on ce.id = cs.event_id
         join leaderboard l on cc.challenge_id = l.challenge_id
         join (select be.leaderboard_id as id, count(be.id) as total from leaderboard join board_entry be on leaderboard.id = be.leaderboard_id group by be.leaderboard_id) ranks on l.id = ranks.id
         join board_entry be on l.id = be.leaderboard_id
where type='Daily' and cc.ended = true;