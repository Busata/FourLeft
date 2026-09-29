#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# Profile disputes (V038/V039): mint an admin link to the disputes page. Same model as tiers.sh — no CLI-facing
# HTTP endpoint (minting over HTTP would need a secret the bot doesn't have); we insert straight into the backend
# DB over ssh veevi -> docker exec -> psql. The admin_link UUID is the credential and expires after 7 days.
DB_CONTAINER="db.backend-ea-sports-wrc"
DB_USER="backendeasportswrc"
DB_NAME="backendeasportswrc"
BASE_URL="https://fourleft.io/easportswrc/admin"

function runSql() {
  ssh veevi "docker exec -i $DB_CONTAINER psql -tA -U $DB_USER -d $DB_NAME"
}

function firstUuid() {
  grep -Eio '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}' | head -n1
}

function mintDisputesLink() {
  local link_id
  link_id=$(runSql <<'SQL' | firstUuid
INSERT INTO admin_link (id, created_by, created_at, expires_at)
VALUES (gen_random_uuid(), 'cli', now() AT TIME ZONE 'UTC', (now() AT TIME ZONE 'UTC') + interval '7 days')
RETURNING id;
SQL
)
  if [[ -z "$link_id" ]]; then
    echo "Could not create an admin link."
    return 1
  fi
  echo "Disputes page (valid for 7 days):"
  echo "$BASE_URL/$link_id/disputes"
}

function listDisputes() {
  echo "Open disputes (racenet | holder discord id | disputer discord id | since):"
  runSql <<'SQL'
SELECT racenet || ' | ' || coalesce(discord_id, '-') || ' | ' || disputed_by_discord_id || ' | ' || to_char(disputed_at, 'YYYY-MM-DD HH24:MI')
FROM profile WHERE claim_state = 'DISPUTED' ORDER BY disputed_at;
SQL
}

TITLE="Profile Dispute Tasks"
LINK="Generate a link to the disputes page"
LIST="List open disputes"
TYPES=("$LINK" "$LIST")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          mintDisputesLink
            ;;
        2)
          listDisputes
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi
