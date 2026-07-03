#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# Time-trial probe launcher. There is deliberately no public HTTP endpoint for this: launching a
# probe = inserting PENDING job rows into the backend DB, which the running QueueWorker then claims
# and runs (job type TT_PROBE, one job per rally). Reaches the DB the same way database.sh does:
# ssh veevi -> docker exec -> psql (local trust auth inside the container, no password).
DB_CONTAINER="db.backend-ea-sports-wrc"
DB_USER="backendeasportswrc"
DB_NAME="backendeasportswrc"

# Enqueue one probe job per rally, skipping rallies that already have a probe job pending/running.
function enqueueProbeAll() {
  echo "Enqueuing a time-trial probe job per rally..."
  ssh veevi "docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME" <<'SQL'
INSERT INTO job (id, type, ref)
SELECT nextval('job_seq'), 'TT_PROBE', c.location_id::text
FROM (SELECT DISTINCT location_id FROM time_trial_combination) c
WHERE NOT EXISTS (
    SELECT 1 FROM job j
    WHERE j.type = 'TT_PROBE' AND j.ref = c.location_id::text AND j.status IN ('PENDING', 'RUNNING')
);
SQL
  echo "Done. Watch progress on the work-queue status page (type: Time-trial probe)."
}

# Enqueue a probe job for a single rally (location id), unless one is already in flight for it.
function enqueueProbeRally() {
  local location_id="$1"
  if ! [[ "$location_id" =~ ^[0-9]+$ ]]; then
    echo "Invalid location id: '$location_id' (must be numeric)."
    return 1
  fi
  echo "Enqueuing a time-trial probe job for location $location_id..."
  ssh veevi "docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME" <<SQL
INSERT INTO job (id, type, ref)
SELECT nextval('job_seq'), 'TT_PROBE', '$location_id'
WHERE NOT EXISTS (
    SELECT 1 FROM job j
    WHERE j.type = 'TT_PROBE' AND j.ref = '$location_id' AND j.status IN ('PENDING', 'RUNNING')
);
SQL
  echo "Done."
}

TITLE="Time Trial Tasks"
PROBE_ALL="Probe all boards (one job per rally)"
PROBE_RALLY="Probe a single rally (enter location id)"
TYPES=("$PROBE_ALL" "$PROBE_RALLY")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          enqueueProbeAll
            ;;
        2)
          location_id=$(whiptail --inputbox "Location id to probe (e.g. 17 = Rallye Monte-Carlo)" 10 60 3>&2 2>&1 1>&3)
          [ -n "$location_id" ] && enqueueProbeRally "$location_id"
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi
