#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# Channel-configuration link generator. Mirrors timetrials.sh: there is deliberately no CLI-facing HTTP
# endpoint. Minting a link = inserting a channel_configuration_request row, whose UUID id is the
# shareable token. We create it straight in the backend DB over ssh veevi -> docker exec -> psql (local
# trust auth inside the container, no password), so nothing new is exposed to the public internet. The
# only public endpoint is the read-only GET /api_v2/configuration/channel/{token}, where possession of
# the unguessable UUID is the credential (same model as the profile-edit links). guild_id is backfilled
# from discord_club_configuration when the channel is already tracked, and stays NULL otherwise (the
# view page only shows the channel id).
DB_CONTAINER="db.backend-ea-sports-wrc"
DB_USER="backendeasportswrc"
DB_NAME="backendeasportswrc"
BASE_URL="https://fourleft.io/easportswrc/channel"

# Create a config-link token for a Discord channel id and print the shareable URL. gen_random_uuid()
# is core in Postgres 16; RETURNING + psql -tA prints just the UUID for capture. channel_id is
# validated numeric before interpolation, so the heredoc can't be used for injection.
function generateChannelLink() {
  local channel_id="$1"
  if ! [[ "$channel_id" =~ ^[0-9]{17,20}$ ]]; then
    echo "Invalid channel id: '$channel_id' (expected a 17-20 digit Discord snowflake)."
    return 1
  fi

  local request_id
  request_id=$(ssh veevi "docker exec -i $DB_CONTAINER psql -tA -U $DB_USER -d $DB_NAME" <<SQL
INSERT INTO channel_configuration_request (id, guild_id, channel_id, discord_id, requested_time)
VALUES (
    gen_random_uuid(),
    (SELECT guild_id FROM discord_club_configuration WHERE channel_id = $channel_id LIMIT 1),
    $channel_id,
    'cli',
    now()
)
RETURNING id;
SQL
)

  request_id=$(echo "$request_id" | tr -d '[:space:]')

  if [[ -z "$request_id" ]]; then
    echo "Failed to create a configuration link (no id returned)."
    return 1
  fi

  echo "Configuration link for channel $channel_id:"
  echo "$BASE_URL/$request_id"
}

TITLE="Channel Configuration Tasks"
GENERATE_LINK="Generate a config link (enter channel id)"
TYPES=("$GENERATE_LINK")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          channel_id=$(whiptail --inputbox "Discord channel id" 10 60 3>&2 2>&1 1>&3)
          [ -n "$channel_id" ] && generateChannelLink "$channel_id"
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi
