alter table message_log add column author text;
update message_log set author='';