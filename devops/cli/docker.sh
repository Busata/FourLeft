#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

function startDocker() {
  local local_docker=$1;

  docker compose -f "$FOURLEFT_DEVOPS_ROOT"/docker/"$local_docker".yml up -d
}


TITLE="Database Tasks"
START_LOCAL_DB_EASPORTSWRC="Start local DB - EA Sports WRC"
START_RABBITMQ="Start RabbitMQ"
START_LOCAL_DB_DISCORD="Start local DB - Discord bot"

TYPES=("$START_LOCAL_DB_EASPORTSWRC" "$START_RABBITMQ" "$START_LOCAL_DB_DISCORD")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          startDocker "backend-ea-sports-wrc"
            ;;
        2)
          startDocker "rabbitmq"
            ;;
        3)
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi