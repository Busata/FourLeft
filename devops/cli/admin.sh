#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# AC Rally administrator management. Admin is a role on app_user (role='ADMIN'); there is no
# self-service path, so the first administrator is bootstrapped here by promoting an already
# registered user by email. Reaches the DB the same way clubs.sh / channel.sh do:
# ssh veevi -> docker exec -> psql (local trust auth inside the container, no password).
#
# NOTE: AC Rally has its own database container, separate from the EA Sports WRC one. The name
# below follows the db.backend-ea-sports-wrc convention; confirm it against the prod
# docker-compose.yml on veevi and adjust this single variable if it differs.
DB_CONTAINER="db.backend-acrally"
DB_USER="backendacrally"
DB_NAME="backendacrally"

# Email is single-quoted into the SQL; validate it first so the heredoc can't be used for
# injection. Deliberately permissive (one @, no quotes/whitespace/backslashes) — real addresses
# already registered will pass; anything that could break out of the quoted literal is rejected.
function validEmail() {
  [[ "$1" =~ ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$ ]]
}

# Flip a user's role and print the affected email (empty output = no such user).
function setRole() {
  local email="$1"
  local role="$2"
  if ! validEmail "$email"; then
    echo "Invalid email: '$email'."
    return 1
  fi
  local result
  result=$(ssh veevi "docker exec -i $DB_CONTAINER psql -tA -U $DB_USER -d $DB_NAME" <<SQL
UPDATE app_user SET role = '$role' WHERE lower(email) = lower('$email') RETURNING email;
SQL
)
  if [ -n "$result" ]; then
    echo "OK: '$result' role set to $role."
  else
    echo "No user found with email '$email' — nothing changed."
  fi
}

function listAdmins() {
  echo "Current administrators:"
  ssh veevi "docker exec -i $DB_CONTAINER psql -tA -F ' | ' -U $DB_USER -d $DB_NAME" <<'SQL'
SELECT email, display_name FROM app_user WHERE role = 'ADMIN' ORDER BY email;
SQL
}

TITLE="AC Rally — Admin Users"
PROMOTE="Promote user to admin (enter email)"
DEMOTE="Demote admin to user (enter email)"
LIST="List administrators"
TYPES=("$PROMOTE" "$DEMOTE" "$LIST")

selected_option_index=$(selectMenu "$TITLE" "${TYPES[@]}")

if [ -n "$selected_option_index" ]; then
    case $selected_option_index in
        1)
          email=$(whiptail --inputbox "Email of the registered user to promote to admin" 10 70 3>&2 2>&1 1>&3)
          [ -n "$email" ] && setRole "$email" "ADMIN"
            ;;
        2)
          email=$(whiptail --inputbox "Email of the admin to demote to regular user" 10 70 3>&2 2>&1 1>&3)
          [ -n "$email" ] && setRole "$email" "USER"
            ;;
        3)
          listAdmins
            ;;
        *)
            echo "Invalid selection or cancelled."
            ;;
    esac
fi
