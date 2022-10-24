echo "Create remote backup...."
ssh veevi /bin/bash << EOF
docker exec -t db.fourleft_discord pg_dumpall --no-owner --no-role-passwords -w -x -c -U postgres > dump_fourleft_discord.sql;
EOF

echo "Copying to local machine..."
scp veevi:~/dump_fourleft_discord.sql fourleft_discord.sql

echo "Start up project postgresql docker.."
(docker-compose down && docker-compose up -d)

echo "Restore dump."
sleep 5s

cat fourleft_discord.sql | docker exec -i db.fourleft.discord.local /bin/bash -c 'PGPASSWORD=fourleft_discord psql -U fourleft_discord'

(docker-compose stop)

rm fourleft_discord.sql

echo "Done"
