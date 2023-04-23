create materialized view unique_players as SELECT name,
                                                  count(*) occurrence
                                           FROM board_entry
                                           GROUP BY name
                                           ORDER BY occurrence DESC;