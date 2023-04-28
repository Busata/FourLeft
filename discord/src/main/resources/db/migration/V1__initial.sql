CREATE table if not exists auto_post_tracking
(
    id UUID not null primary key,

    event_id varchar(255) not null,
    challenge_id varchar(255) not null,

    entry_count bigint,

    member_list text

);

create table if not exists field_mapping (
  id UUID not null primary key,
  name varchar(255),
  value text,
  type varchar(255)
)
