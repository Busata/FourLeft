#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

TITLE="Fourleft Tasks"
DEPLOY="Build & deploy"
RESTORE_DATA="Restore data"
FOLLOW_LOGS="Follow logs"
LOCAL_DOCKER="Local docker actions"
TYPES=("$DEPLOY" "$RESTORE_DATA" "$FOLLOW_LOGS" "$LOCAL_DOCKER")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/deploy.sh
            ;;
        2)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/database.sh
            ;;
        3)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/logs.sh
            ;;
        4)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/docker.sh
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi