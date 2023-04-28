insert into point_system values (uuid_generate_v4(), 'GRF Special', 0, 1);

update fixed_points_calculator set point_system_id=(select id from point_system where description='GRF Special') where id='4adb8d55-2666-4045-b785-1ea48c939e36';

insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),1,25);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),2,21);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),3,18);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),4,16);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),5,15);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),6,14);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),7,13);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),8,12);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),9,11);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),10,10);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),11,9);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),12,8);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),13,7);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),14,6);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),15,5);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),16,4);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),17,3);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),18,2);
insert into point_system_ranking_points values ((select id from point_system where description='GRF Special'),19,1);

insert into point_system_power_stage_points values ((select id from point_system where description='GRF Special'),1,5);
insert into point_system_power_stage_points values ((select id from point_system where description='GRF Special'),2,4);
insert into point_system_power_stage_points values ((select id from point_system where description='GRF Special'),3,3);
insert into point_system_power_stage_points values ((select id from point_system where description='GRF Special'),4,2);
insert into point_system_power_stage_points values ((select id from point_system where description='GRF Special'),5,1);



insert into point_system values (uuid_generate_v4(), 'Unite', 0, 0);

insert into point_system_ranking_points values ((select id from point_system where description='Unite'),1,25);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),2,18);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),3,15);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),4,12);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),5,10);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),6,8);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),7,6);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),8,4);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),9,2);
insert into point_system_ranking_points values ((select id from point_system where description='Unite'),10,1);

insert into point_system_power_stage_points values ((select id from point_system where description='Unite'),1,5);
insert into point_system_power_stage_points values ((select id from point_system where description='Unite'),2,4);
insert into point_system_power_stage_points values ((select id from point_system where description='Unite'),3,3);
insert into point_system_power_stage_points values ((select id from point_system where description='Unite'),4,2);
insert into point_system_power_stage_points values ((select id from point_system where description='Unite'),5,1);

insert into points_calculator values (uuid_generate_v4());
insert into fixed_points_calculator values ((select id from points_calculator where id not in
                                                                            ('ecc898ea-9b2a-456d-92a0-1105c12acf46',
                                                                             '4adb8d55-2666-4045-b785-1ea48c939e36',
                                                                             '96066345-08eb-4d6e-bd04-d3b16f2cf26e',
                                                                             'd0399fbe-a0ca-41af-83c1-b84d075ee46a'
                                                                                )), 1, null, (select id from point_system where description='Unite'));


update club_view set points_calculator_id=(select id from points_calculator where id not in
                                                                                  ('ecc898ea-9b2a-456d-92a0-1105c12acf46',
                                                                                   '4adb8d55-2666-4045-b785-1ea48c939e36',
                                                                                   '96066345-08eb-4d6e-bd04-d3b16f2cf26e',
                                                                                   'd0399fbe-a0ca-41af-83c1-b84d075ee46a'))
                                                                                       where id='5df64a07-dcbc-4bad-9805-3ce91253429a';

update single_club_view set use_power_stage=true where id='5c3bda2a-f335-4a29-82a0-c85c6a137153';
update single_club_view set default_powerstage_index=-1 where id='5c3bda2a-f335-4a29-82a0-c85c6a137153';