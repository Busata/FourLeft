#!/bin/bash
function selectMenu() {
    local title="$1"
    shift  # Skip the first argument, as it's already saved in 'title'
    local options=()
    local index=1

    term_width=$(tput cols)
    term_height=$(tput lines)

    width=$((term_width * 3 / 4))
    height=$((term_height * 3 / 4))

    # Loop through the remaining arguments and add them to the options array
    for opt in "$@"; do
        options+=("$index" "$opt")
        ((index++))
    done

    # Use 'whiptail' to generate the menu
    OPTION=$(whiptail  --title "$title" --menu "Selection menu" $height $width 5 \
        "${options[@]}" 3>&2 2>&1 1>&3)

    echo "$OPTION"
}

# Multi-select menu. Pass the title followed by repeating (tag, label, status)
# triples (status = on/off). Prints the selected tags, space-separated, unquoted.
function selectChecklist() {
    local title="$1"
    shift

    term_width=$(tput cols)
    term_height=$(tput lines)
    width=$((term_width * 3 / 4))
    height=$((term_height * 3 / 4))

    local raw
    raw=$(whiptail --title "$title" --checklist "Space toggles, Enter confirms" \
        $height $width 10 "$@" 3>&2 2>&1 1>&3)

    # whiptail returns tags wrapped in quotes: "a" "b" -> strip them.
    echo "$raw" | tr -d '"'
}
