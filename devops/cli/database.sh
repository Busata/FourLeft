#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

function restoreDatabase() {
  echo "Creating remote backup...."
  local remote_database=$1;
  local user=$2;
  local local_database=$3;
  local
  ssh veevi "/bin/bash -c 'docker exec -t $remote_database pg_dumpall --no-owner --no-role-passwords -w -x -c -U $user > dump_$remote_database.sql;gzip -f dump_$remote_database.sql'";

  echo "Copying to local machine..."
  scp veevi:~/dump_"$remote_database".sql.gz /tmp/dump.sql.gz

  echo "Unzipping file..."
  gzip -d /tmp/dump.sql.gz

  echo "(Re)creating container..."
  docker compose -f "$FOURLEFT_DEVOPS_ROOT"/docker/"$local_database".yml down
  docker compose -f "$FOURLEFT_DEVOPS_ROOT"/docker/"$local_database".yml up -d

  echo "Restore dump..."
  sleep 5s

  cat /tmp/dump.sql | docker exec -i db.local.$local_database /bin/bash -c "PGPASSWORD=$user psql -U $user $user"

  echo "Stopping container.."
  docker compose -f "$FOURLEFT_DEVOPS_ROOT"/database/"$local_database".yml stop

  echo "Removing local sql file..."
  rm /tmp/dump.sql



#
#
#  echo "Start up project postgresql docker.."
#  (docker-compose down && docker-compose up -d)
#
#  echo "Restore dump."
#  sleep 5s
#
#  cat fourleft.sql | docker exec -i db.fourleft.local /bin/bash -c 'PGPASSWORD=fourleft psql -U fourleft'
#
#  (docker-compose stop)
#
#  rm fourleft.sql
#
#  echo "Done"

  true;
}


TITLE="Database Tasks"
RESTORE_EASPORTSWRC="Restore to local DB - EA Sports WRC"
RESTORE_DIRTRALLYTWO="Restore to local DB - Dirt Rally 2.0"
RESTORE_DISCORD="Restore to local DB - Discord bot"

TYPES=("$RESTORE_EASPORTSWRC" "$RESTORE_DIRTRALLYTWO" "$RESTORE_DISCORD")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          restoreDatabase "db.backend-ea-sports-wrc" "backendeasportswrc" "backend-ea-sports-wrc"
            ;;
        2)
          restoreDatabase "db.fourleft" "postgres"
            ;;
        3)
          restoreDatabase "db.fourleft_discord" "postgres"
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi