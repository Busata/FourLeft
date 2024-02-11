
function followLogs()
{
  ssh veevi "/bin/bash -c 'docker logs $1 --follow'"
  true
}

TITLE="Log Tasks"
RESTORE_EASPORTSWRC="Follow logs -- Backend - EA SPORTS WRC"
RESTORE_AUTHENTICATOR="Follow logs -- Backend - Authenticator"
RESTORE_DISCORD="Follow logs -- Backend - Discord"
RESTORE_WRC="Follow logs -- Backend -- WRC"

TYPES=("$RESTORE_EASPORTSWRC" "$RESTORE_AUTHENTICATOR" "$RESTORE_DISCORD" "$RESTORE_WRC")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          followLogs "spring.backend-ea-sports-wrc"
            ;;
        2)
          followLogs "spring.racenet-authenticator"
            ;;
        3)
          followLogs "spring.backend-wrc"
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi