#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

TITLE="Fourleft Tasks"
DEPLOY="Build & deploy"
RESTORE_DATA="Restore data"
FOLLOW_LOGS="Follow logs"
LOCAL_DOCKER="Local docker actions"
TIME_TRIALS="Time trial actions"
CHANNEL="Channel configuration actions"
CLUB_EXPORT="Club export actions"
RELEASE_AGENT="Release AC Rally agent"
TYPES=("$DEPLOY" "$RESTORE_DATA" "$FOLLOW_LOGS" "$LOCAL_DOCKER" "$TIME_TRIALS" "$CHANNEL" "$CLUB_EXPORT" "$RELEASE_AGENT")

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
        5)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/timetrials.sh
            ;;
        6)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/channel.sh
            ;;
        7)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/clubs.sh
            ;;
        8)
          . "$FOURLEFT_DEVOPS_ROOT"/cli/release-agent.sh
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi