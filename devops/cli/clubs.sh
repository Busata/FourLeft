#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# Club results export launcher. Like the time-trial launchers, this enqueues PENDING job rows into the
# backend DB (job type CLUB_EXPORT, one job per club) which the running QueueWorker then claims and
# runs — each job regenerates one club's cached summary JSON in the export directory. Normally this
# happens on the hourly schedule; this is the manual trigger (e.g. right after a restart, before the
# next hourly tick). Reaches the DB the same way timetrials.sh / database.sh do: ssh veevi ->
# docker exec -> psql (local trust auth inside the container, no password).
DB_CONTAINER="db.backend-ea-sports-wrc"
DB_USER="backendeasportswrc"
DB_NAME="backendeasportswrc"

# Enqueue one export job per club, skipping clubs that already have an export job pending/running.
function enqueueExportAll() {
  echo "Enqueuing a club export job per club..."
  ssh veevi "docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME" <<'SQL'
INSERT INTO job (id, type, ref)
SELECT nextval('job_seq'), 'CLUB_EXPORT', c.id
FROM club c
WHERE NOT EXISTS (
    SELECT 1 FROM job j
    WHERE j.type = 'CLUB_EXPORT' AND j.ref = c.id AND j.status IN ('PENDING', 'RUNNING')
);
SQL
  echo "Done. Watch progress on the work-queue status page (type: Club export)."
}

# Enqueue an export job for one specific club (club id, e.g. "146"), unless one is already in flight.
function enqueueExportClub() {
  local club_id="$1"
  if ! [[ "$club_id" =~ ^[A-Za-z0-9_-]+$ ]]; then
    echo "Invalid club id: '$club_id'."
    return 1
  fi
  echo "Enqueuing a club export job for club $club_id..."
  ssh veevi "docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME" <<SQL
INSERT INTO job (id, type, ref)
SELECT nextval('job_seq'), 'CLUB_EXPORT', '$club_id'
WHERE EXISTS (SELECT 1 FROM club WHERE id = '$club_id')
AND NOT EXISTS (
    SELECT 1 FROM job j
    WHERE j.type = 'CLUB_EXPORT' AND j.ref = '$club_id' AND j.status IN ('PENDING', 'RUNNING')
);
SQL
  echo "Done (INSERT 0 1 = enqueued; INSERT 0 0 = unknown club or already in flight)."
}

TITLE="Club Export Tasks"
EXPORT_ALL="Export all clubs (one job per club)"
EXPORT_CLUB="Export a single club (enter club id)"
TYPES=("$EXPORT_ALL" "$EXPORT_CLUB")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          enqueueExportAll
            ;;
        2)
          club_id=$(whiptail --inputbox "Club id to export (e.g. 146)" 10 60 3>&2 2>&1 1>&3)
          [ -n "$club_id" ] && enqueueExportClub "$club_id"
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi
