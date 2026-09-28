#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# Tier sets (V037): create one, or mint a new edit link for an existing one. Same model as channel.sh —
# no CLI-facing HTTP endpoint; we insert straight into the backend DB over ssh veevi -> docker exec ->
# psql, and the tier_set_link UUID is the credential for the public edit page.
DB_CONTAINER="db.backend-ea-sports-wrc"
DB_USER="backendeasportswrc"
DB_NAME="backendeasportswrc"
BASE_URL="https://fourleft.io/easportswrc/tiers"

function runSql() {
  ssh veevi "docker exec -i $DB_CONTAINER psql -tA -U $DB_USER -d $DB_NAME"
}

function firstUuid() {
  grep -Eio '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}' | head -n1
}

# Name is single-quoted into the SQL, so only a safe character set is allowed. Guild id is optional: set it
# to make the set reachable from /fourleft tiers edit in that server (names are unique per guild there).
function createTierSet() {
  local name="$1"
  local guild_id="$2"
  if ! [[ "$name" =~ ^[A-Za-z0-9\ ._#\&+()-]{1,100}$ ]]; then
    echo "Invalid name: '$name' (1-100 characters: letters, digits, spaces and . _ # & + ( ) -)."
    return 1
  fi
  local guild_sql="NULL"
  if [ -n "$guild_id" ]; then
    if ! [[ "$guild_id" =~ ^[0-9]{17,20}$ ]]; then
      echo "Invalid guild id: '$guild_id' (expected a 17-20 digit Discord snowflake)."
      return 1
    fi
    guild_sql="$guild_id"
  fi

  local link_id
  link_id=$(runSql <<SQL | firstUuid
WITH new_set AS (
    INSERT INTO tier_set (id, name, guild_id, created_by, created_at)
    SELECT gen_random_uuid(), '$name', $guild_sql, 'cli', now()
    WHERE $guild_sql IS NULL
       OR NOT EXISTS (SELECT 1 FROM tier_set WHERE guild_id = $guild_sql AND lower(name) = lower('$name'))
    RETURNING id
)
INSERT INTO tier_set_link (id, tier_set_id, discord_id, created_at)
SELECT gen_random_uuid(), id, 'cli', now() FROM new_set
RETURNING id;
SQL
)
  if [[ -z "$link_id" ]]; then
    echo "Nothing created: guild $guild_id already has a tier set named '$name'."
    return 1
  fi
  echo "Tier set '$name' created. Edit link:"
  echo "$BASE_URL/$link_id"
}

# Pick an existing set from a menu and mint a fresh edit link for it.
function linkExistingTierSet() {
  local rows
  rows=$(runSql <<'SQL'
SELECT id || '|' || name || ' (' || coalesce(guild_id::text, 'no guild') || ', ' || to_char(created_at, 'YYYY-MM-DD') || ')'
FROM tier_set ORDER BY created_at DESC;
SQL
)
  if [ -z "$rows" ]; then
    echo "There are no tier sets yet."
    return 1
  fi

  local ids=() labels=()
  while IFS='|' read -r id label; do
    ids+=("$id")
    labels+=("$label")
  done <<< "$rows"

  local index
  index=$(selectMenu "Pick a tier set" "${labels[@]}")
  [ -z "$index" ] && return 1
  local tier_set_id="${ids[$((index - 1))]}"

  local link_id
  link_id=$(runSql <<SQL | firstUuid
INSERT INTO tier_set_link (id, tier_set_id, discord_id, created_at)
VALUES (gen_random_uuid(), '$tier_set_id', 'cli', now())
RETURNING id;
SQL
)
  echo "Edit link for ${labels[$((index - 1))]}:"
  echo "$BASE_URL/$link_id"
}

TITLE="Tier Set Tasks"
CREATE="Create a tier set (enter name, optional guild id)"
LINK="Generate an edit link for an existing tier set"
TYPES=("$CREATE" "$LINK")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          name=$(whiptail --inputbox "Tier set name, e.g. JRC" 10 60 3>&2 2>&1 1>&3)
          [ -z "$name" ] && exit 0
          guild_id=$(whiptail --inputbox "Discord guild id (optional; lets /fourleft tiers edit find it)" 10 70 3>&2 2>&1 1>&3)
          createTierSet "$name" "$guild_id"
            ;;
        2)
          linkExistingTierSet
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi
