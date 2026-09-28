#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# Delete a message the bot posted (e.g. a duplicate autopost). Discord needs the channel as well as the
# message id, so this takes a message link (right-click > Copy Message Link) or "<channelId> <messageId>".
# The call runs on veevi with the backend container's bot token, so the token never leaves the server.
# autopost_entry rows are left alone: they record the entries as posted, so the next sync won't repost them.
BACKEND_CONTAINER="spring.backend-ea-sports-wrc"

function deleteBotMessage() {
  local input="$1"
  local channel_id message_id
  if [[ "$input" =~ discord(app)?\.com/channels/[0-9@me]+/([0-9]{17,20})/([0-9]{17,20}) ]]; then
    channel_id="${BASH_REMATCH[2]}"
    message_id="${BASH_REMATCH[3]}"
  elif [[ "$input" =~ ^[[:space:]]*([0-9]{17,20})[[:space:]/]+([0-9]{17,20})[[:space:]]*$ ]]; then
    channel_id="${BASH_REMATCH[1]}"
    message_id="${BASH_REMATCH[2]}"
  else
    echo "Expected a Discord message link or '<channelId> <messageId>', got: '$input'"
    return 1
  fi

  local status
  status=$(ssh veevi "/bin/bash -s" <<REMOTE
token=\$(docker exec $BACKEND_CONTAINER printenv | grep -iE '^DISCORD[_.]BOT[_-]?TOKEN=' | head -n1 | cut -d= -f2-)
if [ -z "\$token" ]; then
  echo "no-token"
  exit 0
fi
curl -s -o /dev/null -w '%{http_code}' -X DELETE \
  -H "Authorization: Bot \$token" \
  "https://discord.com/api/v10/channels/$channel_id/messages/$message_id"
REMOTE
)

  case "$status" in
    204) echo "Deleted message $message_id in channel $channel_id." ;;
    404) echo "Not found: message $message_id in channel $channel_id (already deleted, or wrong channel)." ;;
    403) echo "Forbidden: the bot can't delete message $message_id (not its own, or no access to the channel)." ;;
    no-token) echo "Couldn't find the bot token in $BACKEND_CONTAINER's environment." ;;
    *) echo "Discord answered '$status' for message $message_id in channel $channel_id." ;;
  esac
}

input=$(whiptail --inputbox "Message link, or '<channelId> <messageId>'" 10 80 3>&2 2>&1 1>&3)
[ -z "$input" ] && exit 0
deleteBotMessage "$input"
