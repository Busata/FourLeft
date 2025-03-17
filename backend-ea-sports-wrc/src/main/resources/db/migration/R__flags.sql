delete from field_mapping where name like 'nationalityFlag%';
delete from field_mapping where name like 'eventFlag%';
delete from field_mapping where name like 'platform%';

insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platform#1', '<:xbox:894882459664138271>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platform#2', '<:playstation:894882472788115456>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platform#3', '<:steam:894882488726474793>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platform#6', ':computer:', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platform#7', ':computer:', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platform#14', '<:ea:894883713031225405>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platform#65', ':computer:', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platform#128', '<:steam:894882488726474793>', '', true, 'DISCORD', 'EMOTE');

insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#PC', ':computer:', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#PLAYSTATION', '<:playstation:894882472788115456>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#XBOX', '<:xbox:894882459664138271>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#EPIC', '<:epic:1170397451186872320>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#EA_APP', '<:ea:894883713031225405>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#STEAM', '<:steam:894882488726474793>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#STEAM_DECK', '<:steam:894882488726474793>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#OTHER', '<:blank:894976571406966814>', '', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'platformEnum#UNKNOWN', '<:blank:894976571406966814>', '', true, 'DISCORD', 'EMOTE');


insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#5', ':flag_fr:', 'Rally Mediterraneo', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#6', ':flag_pt:', 'Portugal', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#7', ':flag_it:', 'Rally Italia Sardegna', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#8', ':flag_ee:', 'Rally Estonia', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#9', ':flag_no:', 'Rally Scandia', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#12', ':flag_mx:', 'Guanajuato Rally México', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#13', ':flag_cl:', 'Bio Bío Rally Chile', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#14', ':flag_id:', 'Pacifico', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#15', ':flag_fi:', 'Rally Finland', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#16', ':flag_hr:', 'Croatia', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#17', ':flag_mc:', 'Monte Carlo', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#18', ':flag_se:', 'Rally Sweden', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#24', ':flag_gr:', 'Greece', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#25', ':flag_jp:', 'FORUM8 Rally Japan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#26', ':flag_ke:', 'Safari Rally Kenya', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#27', ':flag_nz:', 'FANATEC Rally Oceania', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#28', ':flag_es:', 'Rally Iberia', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#29', ':flag_eu:', 'CER', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#30', ':flag_lv:', 'Rally Latvia', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'eventFlag#31', ':flag_pl:', 'Rally Poland', true, 'DISCORD', 'EMOTE');


insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#1', ':flag_hu:', 'Hungarian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#2', ':flag_pt:', 'Portuguese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#6', ':flag_it:', 'Italian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#7', ':flag_fr:', 'French', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#8', ':flag_ci:', 'Côte dIvoire', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#9', ':flag_no:', 'Norwegian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#10',':globe_with_meridians:', 'Northern Ireland', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#11',':flag_cz:', 'Czech Republic', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#12',':flag_cl:', 'Chilean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#13',':flag_mx:', 'Mexican', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#14',':flag_id:', 'Indonesian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#15',':flag_jp:', 'Japanese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#16',':flag_white:','Russian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#17',':flag_gb:', 'United Kingdom', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#18',':flag_es:', 'Spanish', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#19',':flag_de:', 'German', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#20',':flag_us:', 'American', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#21',':flag_kr:', 'South Korea', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#22',':flag_my:', 'Malaysian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#23',':england:', 'English', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#24',':scotland:', 'Scottish', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#25',':flag_ca:', 'Canadian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#26',':flag_be:', 'Belgian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#27',':flag_tr:', 'Turkish', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#28',':flag_fi:', 'Finnish', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#29',':flag_br:', 'Brazilian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#30',':flag_nl:', 'Dutch', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#31',':flag_ie:', 'Irish', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#32',':flag_at:', 'Austrian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#33',':flag_gr:', 'Greek', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#34',':flag_lu:', 'Luxembourger', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#35',':flag_si:', 'Slovenian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#36',':flag_cy:', 'Cypriot', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#37',':flag_au:', 'Australian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#38',':flag_ar:', 'Argentinian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#39',':flag_bg:', 'Bulgarian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#40',':flag_cn:', 'China', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#41',':flag_hr:', 'Croatian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#42',':flag_dk:', 'Danish', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#43',':flag_ee:', 'Estonian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#44',':flag_sk:', 'Slovakian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#45',':flag_is:', 'Icelander', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#46',':flag_in:', 'Indian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#47',':flag_jm:', 'Jamaican', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#48',':flag_jo:', 'Jordanian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#49',':flag_lv:', 'Latvian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#50',':flag_lt:', 'Lithuanian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#51',':flag_nz:', 'New Zealand', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#52',':flag_pk:', 'Pakistani', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#53',':flag_pl:', 'Polish', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#54',':flag_ro:', 'Romanian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#55',':flag_sa:', 'Saudi Arabia', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#56',':flag_za:', 'South Africa', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#57',':flag_ch:', 'Swiss', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#58',':flag_th:', 'Thai', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#59',':wales:󠁧󠁢󠁷󠁬󠁳󠁿', 'Welsh', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#60',':flag_bh:', 'Bahraini', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#61',':flag_rs:', 'Serbian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#62',':flag_se:', 'Swedish', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#63',':flag_ae:', 'Emirian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#64',':flag_hk:', 'Hong Kong', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#65',':flag_ec:', 'Ecuadorean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#66',':flag_pr:', 'Puerto Rico', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#67',':flag_ve:', 'Venezuelan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#68',':flag_gh:', 'Ghanaian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#69',':flag_cm:', 'Camerounaise', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#70',':flag_kw:', 'Kuwaiti', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#71',':flag_im:', 'Isle of Man', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#72',':flag_om:', 'Omani', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#73',':flag_qa:', 'Qatari', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#74',':flag_ye:', 'Yemeni', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#75',':flag_ng:', 'Nigerian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#76',':flag_ke:', 'Kenyan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#77',':flag_mc:', 'Monégasque', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#78',':flag_ua:', 'Ukrainian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#79',':flag_il:', 'Israeli', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#80',':flag_co:', 'Colombian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#81',':flag_ps:', 'Palestinian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#82',':flag_cr:', 'Costa Rica', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#83',':flag_uy:', 'Uruguayan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#84',':flag_py:', 'Paraguayan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#85',':flag_gb:', 'British', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#86',':flag_bo:', 'Bolivian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type) values (uuid_generate_v4(),'nationalityFlag#87',':flag_pe:', 'Peruvian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#88',':flag_zm:', 'Zambian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#88',':flag_zm:', 'Zambian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#89',':flag_eu:', 'Europe', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#90',':flag_bb:', 'Barbados', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#93',':flag_white:', 'ANA', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#94',':flag_kz:', 'Kazakhstan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#95',':flag_sv:', 'Salvadoran', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#96',':flag_gt:', 'Guatemalan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#97',':flag_hn:', 'Honduran', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#98',':flag_mt:', 'Maltese;', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#99',':flag_ni:', 'Nicaraguan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#100',':flag_pa:', 'Panamanian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#101',':flag_sg:', 'Singaporean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#102',':flag_zw:', 'Zimbabwean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#103',':flag_lb:', 'Lebanese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#104',':flag_ad:', 'Andorran', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#105',':flag_mg:', 'Madagascar ;', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#106',':flag_kp:', 'North Korean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#107',':flag_np:', 'Nepalese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#108',':flag_eg:', 'Egyptian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#109',':flag_ir:', 'Iranian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#110',':flag_iq:', 'Iraqi', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#111',':flag_li:', 'Liechtensteiner', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#112',':flag_md:', 'Moldovan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#113',':flag_ma:', 'Moroccan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#114',':flag_ph:', 'Filipino', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#115',':flag_ws:', 'Sammarinese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#116',':flag_tt:', 'Trinidadian and Tobagonian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#117',':flag_tn:', 'Tunisian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#118',':flag_af:', 'Afghan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#119',':flag_al:', 'Albanian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#120',':flag_dz:', 'Algerian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#121',':flag_ao:', 'Angolan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#122',':flag_am:', 'Armenian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#123',':flag_bd:', 'Bangladeshi', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#124',':flag_by:', 'Belarusian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#125',':flag_bz:', 'Belizean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#126',':flag_bj:', 'Beninese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#127',':flag_ba:', 'Bosnian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#128',':flag_bw:', 'Motswana', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#129',':flag_bn:', 'Bruneian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#130',':flag_bf:', 'Burkinabè', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#131',':flag_bi:', 'Burundian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#132',':flag_kh:', 'Cambodian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#133',':flag_cv:', 'Cape Verdean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#134',':flag_cf:', 'Central African', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#135',':flag_td:', 'Chadian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#136',':flag_km:', 'Comorian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#137',':flag_cu:', 'Cuban', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#138',':flag_do:', 'Dominican Republic', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#139',':flag_cg:', 'Congolese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#140',':flag_gq:', 'Equatorial Guinean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#141',':flag_er:', 'Eritrean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#142',':flag_et:', 'Ethiopia', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#143',':flag_fj:', 'Fijian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#144',':flag_ga:', 'Gabonese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#145',':flag_gm:', 'Gambian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#146',':flag_ge:', 'Georgian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#147',':flag_gn:', 'Guinean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#148',':flag_gw:', 'Bissau-Guinean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#149',':flag_ht:', 'Haitian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#150',':flag_xk:', 'Kosovar;', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#151',':flag_kg:', 'Kyrgyz', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#152',':flag_la:', 'Laos', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#153',':flag_ls:', 'Mosotho', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#154',':flag_lr:', 'Liberian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#155',':flag_ly:', 'Libyan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#156',':flag_mw:', 'Malawian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#157',':flag_ml:', 'Malian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#158',':flag_mr:', 'Mauritanian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#159',':flag_mn:', 'Mongolian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#160',':flag_me:', 'Montenegrin', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#161',':flag_mz:', 'Mozambican', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#162',':flag_mm:', 'Myanmar', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#163',':flag_na:', 'Namibian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#164',':flag_ng:', 'Nigerien', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#166',':flag_rw:', 'Rwandan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#167',':flag_sn:', 'Senegalese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#168',':flag_sl:', 'Sierra Leonean', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#169',':flag_so:', 'Somali', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#170',':flag_ss:', 'South Sudanese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#171',':flag_lk:', 'Sri Lankan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#172',':flag_sd:', 'Sudanese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#173',':flag_sr:', 'Surinamese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#174',':flag_sy:', 'Syrian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#175',':flag_tj:', 'Tajikistani', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#176',':flag_tz:', 'Tanzanian', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#177',':flag_tg:', 'Togolese', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#178',':flag_to:', 'Tongan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#179',':flag_tm:', 'Turkmenistani', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#180',':flag_ug:', 'Ugandan', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#181',':flag_uz:', 'Uzbekistani', true, 'DISCORD', 'EMOTE');
insert into field_mapping (id, name, value, note, mapped_by_user, context, type)  values (uuid_generate_v4(),'nationalityFlag#182',':flag_vn:', 'Vietnamese', true, 'DISCORD', 'EMOTE');