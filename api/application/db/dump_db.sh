echo "Create remote backup...."
ssh veevi /bin/bash << EOF
docker exec -t db.fourleft pg_dumpall --no-owner --no-role-passwords -w -x -c -U postgres > dump_fourleft.sql;
EOF

echo "Copying to local machine..."
scp veevi:~/dump_fourleft.sql fourleft.sql

echo "Start up project postgresql docker.."
(docker-compose down && docker-compose up -d)

echo "Restore dump."
sleep 5s

cat fourleft.sql | docker exec -i db.fourleft.local /bin/bash -c 'PGPASSWORD=fourleft psql -U fourleft'

(docker-compose stop)

rm fourleft.sql

echo "Done"
