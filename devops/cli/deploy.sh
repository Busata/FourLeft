#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# Remote docker host + repo path are remembered in .deploy.conf (gitignored).
# On first run we ask for the repo path and save it.
CONF="$FOURLEFT_DEVOPS_ROOT/.deploy.conf"
[ -f "$CONF" ] && source "$CONF"
REMOTE_HOST="${REMOTE_HOST:-veevi}"

if [ -z "$REMOTE_DIR" ]; then
    REMOTE_DIR=$(whiptail --title "Deploy setup" --inputbox \
        "Path to the fourleft repo on $REMOTE_HOST (where docker-compose.yml lives):" \
        10 70 "~/fourleft" 3>&2 2>&1 1>&3)
    [ -z "$REMOTE_DIR" ] && { echo "Cancelled."; return 0 2>/dev/null || exit 0; }
    { echo "REMOTE_HOST=$REMOTE_HOST"; echo "REMOTE_DIR=$REMOTE_DIR"; } > "$CONF"
fi

# Buildable compose services. Tags MUST match service names in docker-compose.yml.
SERVICE_TAGS=(
    "spring.fourleft.backend-ea-sports-wrc"
    "proxy.fourleft_frontend"
    "spring.fourleft.discord"
    "spring.racenet-authenticator"
    "proxy.fourleft-reverse-proxy"
)
SERVICE_LABELS=(
    "Backend - EA Sports WRC"
    "Frontend"
    "Discord bot"
    "Racenet authenticator"
    "Reverse proxy"
)

# Build & (re)start the given space-separated list of services on the remote host.
function runDeploy() {
    local services="$1"
    if [ -z "$services" ]; then
        echo "Nothing selected."
        return
    fi

    if ! whiptail --title "Deploy" --yesno \
        "About to build & deploy on $REMOTE_HOST:\n\n$services\n\nProceed?" 15 72; then
        echo "Cancelled."
        return
    fi

    # -t for a TTY so docker build progress streams live.
    local remote_cmd="cd $REMOTE_DIR && git pull && docker compose build $services && docker compose up -d $services"

    # The reverse proxy resolves the frontend container's IP once at startup. A frontend redeploy
    # recreates that container with a new IP, but `up -d` won't touch the (unchanged) proxy — so it
    # keeps pointing at the dead container (502s) until restarted. Force it whenever the frontend ships.
    if [[ " $services " == *" proxy.fourleft_frontend "* ]]; then
        remote_cmd="$remote_cmd && docker compose restart proxy.fourleft-reverse-proxy"
    fi

    echo "→ $REMOTE_HOST: $remote_cmd"
    ssh -t "$REMOTE_HOST" "/bin/bash -lc '$remote_cmd'"
    echo "Done."
}

# Preset service combos for the quick-build shortcuts. Values MUST match SERVICE_TAGS above
# (which in turn match docker-compose.yml service names).
BACKEND_ONLY="spring.fourleft.backend-ea-sports-wrc"
FRONTEND_PROXY="proxy.fourleft_frontend proxy.fourleft-reverse-proxy"
BACKEND_FRONTEND_PROXY="spring.fourleft.backend-ea-sports-wrc proxy.fourleft_frontend proxy.fourleft-reverse-proxy"

TITLE="Deploy Tasks"
DEPLOY_ALL="Build & deploy ALL"
DEPLOY_BACKEND="Build & deploy backend"
DEPLOY_FRONTEND="Build & deploy frontend + reverse proxy"
DEPLOY_BOTH="Build & deploy backend + frontend + reverse proxy"
DEPLOY_SELECT="Build & deploy selected..."
TYPES=("$DEPLOY_ALL" "$DEPLOY_BACKEND" "$DEPLOY_FRONTEND" "$DEPLOY_BOTH" "$DEPLOY_SELECT")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
            runDeploy "${SERVICE_TAGS[*]}"
            ;;
        2)
            runDeploy "$BACKEND_ONLY"
            ;;
        3)
            runDeploy "$FRONTEND_PROXY"
            ;;
        4)
            runDeploy "$BACKEND_FRONTEND_PROXY"
            ;;
        5)
            checklist=()
            for i in "${!SERVICE_TAGS[@]}"; do
                checklist+=("${SERVICE_TAGS[$i]}" "${SERVICE_LABELS[$i]}" "off")
            done
            selected=$(selectChecklist "Select services to build & deploy" "${checklist[@]}")
            runDeploy "$selected"
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi
