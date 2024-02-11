#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

TITLE="Fourleft Tasks"
RESTORE_DATA="Restore data"
FOLLOW_LOGS="Follow logs"
LOCAL_DOCKER="Local docker actions"
APP_ACTIONS="App actions"
TYPES=("$RESTORE_DATA" "$FOLLOW_LOGS" "$LOCAL_DOCKER" "$APP_ACTIONS")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/database.sh

            ;;
        2)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/logs.sh
            ;;
        3)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/docker.sh
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi