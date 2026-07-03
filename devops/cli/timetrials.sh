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

# Enqueue one fetch job per existing board (ref = combination id), skipping boards that already have a
# fetch job pending/running. "Existing" = the latest probe for the combination found the board — so a
# probe pass must have run first. One job per board keeps each small; the racenet-timetrial rate
# limiter (8/s) governs the overall pace across all of them.
function enqueueFetchAll() {
  echo "Enqueuing a time-trial fetch job per existing board..."
  ssh veevi "docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME" <<'SQL'
INSERT INTO job (id, type, ref)
SELECT nextval('job_seq'), 'TT_FETCH', p.combination_id
FROM (
    SELECT DISTINCT ON (combination_id) combination_id, board_exists
    FROM time_trial_probe
    ORDER BY combination_id, probed_at DESC
) p
WHERE p.board_exists
AND NOT EXISTS (
    SELECT 1 FROM job j
    WHERE j.type = 'TT_FETCH' AND j.ref = p.combination_id AND j.status IN ('PENDING', 'RUNNING')
);
SQL
  echo "Done. Watch progress on the work-queue status page (type: Time-trial fetch)."
}

# Enqueue a fetch job for one specific board (combination id, e.g. "17-252-1-19"), for testing a
# single board end-to-end. Unlike the bulk actions this doesn't require a prior probe — it just needs
# the combination to exist in the catalog; a non-existent board comes back as a 404 the fetch records.
function enqueueFetchBoard() {
  local combination_id="$1"
  if ! [[ "$combination_id" =~ ^[0-9]+-[0-9]+-[0-9]+-[0-9]+$ ]]; then
    echo "Invalid combination id: '$combination_id' (expected location-route-surface-class, e.g. 17-252-1-19)."
    return 1
  fi
  echo "Enqueuing a time-trial fetch job for board $combination_id..."
  ssh veevi "docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME" <<SQL
INSERT INTO job (id, type, ref)
SELECT nextval('job_seq'), 'TT_FETCH', '$combination_id'
WHERE EXISTS (SELECT 1 FROM time_trial_combination WHERE id = '$combination_id')
AND NOT EXISTS (
    SELECT 1 FROM job j
    WHERE j.type = 'TT_FETCH' AND j.ref = '$combination_id' AND j.status IN ('PENDING', 'RUNNING')
);
SQL
  echo "Done (INSERT 0 1 = enqueued; INSERT 0 0 = unknown board or already in flight)."
}

# Enqueue fetch jobs for one rally's existing boards (location id), skipping any already in flight.
function enqueueFetchRally() {
  local location_id="$1"
  if ! [[ "$location_id" =~ ^[0-9]+$ ]]; then
    echo "Invalid location id: '$location_id' (must be numeric)."
    return 1
  fi
  echo "Enqueuing time-trial fetch jobs for location $location_id..."
  ssh veevi "docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME" <<SQL
INSERT INTO job (id, type, ref)
SELECT nextval('job_seq'), 'TT_FETCH', p.combination_id
FROM (
    SELECT DISTINCT ON (combination_id) combination_id, board_exists
    FROM time_trial_probe
    WHERE combination_id IN (SELECT id FROM time_trial_combination WHERE location_id = $location_id)
    ORDER BY combination_id, probed_at DESC
) p
WHERE p.board_exists
AND NOT EXISTS (
    SELECT 1 FROM job j
    WHERE j.type = 'TT_FETCH' AND j.ref = p.combination_id AND j.status IN ('PENDING', 'RUNNING')
);
SQL
  echo "Done."
}

TITLE="Time Trial Tasks"
PROBE_ALL="Probe all boards (one job per rally)"
PROBE_RALLY="Probe a single rally (enter location id)"
FETCH_ALL="Fetch all existing boards (one job per board)"
FETCH_RALLY="Fetch one rally's boards (enter location id)"
FETCH_BOARD="Fetch a single board (enter combination id) — test"
TYPES=("$PROBE_ALL" "$PROBE_RALLY" "$FETCH_ALL" "$FETCH_RALLY" "$FETCH_BOARD")

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
        3)
          enqueueFetchAll
            ;;
        4)
          location_id=$(whiptail --inputbox "Location id to fetch (e.g. 17 = Rallye Monte-Carlo)" 10 60 3>&2 2>&1 1>&3)
          [ -n "$location_id" ] && enqueueFetchRally "$location_id"
            ;;
        5)
          combination_id=$(whiptail --inputbox "Combination id to fetch (location-route-surface-class, e.g. 17-252-1-19)" 10 60 3>&2 2>&1 1>&3)
          [ -n "$combination_id" ] && enqueueFetchBoard "$combination_id"
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi
