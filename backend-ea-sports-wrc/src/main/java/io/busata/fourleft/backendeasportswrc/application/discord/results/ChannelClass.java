package io.busata.fourleft.backendeasportswrc.application.discord.results;

/**
 * One club of a MIXED channel seen as a car class: the tag its entries carry in merged posts — the
 * club's label when set, else its event's car class — and the car class itself for headers. The
 * championship/event ids are the club's own event matched to the primary's (null when unknown); per-club
 * event restrictions target those.
 */
public record ChannelClass(String clubId, String tag, String vehicleClass, String championshipId, String eventId) {

    // Keeps a long class name ("World Rally Car 1997 - 2011") from eating the per-entry length budget.
    static final int MAX_TAG_LENGTH = 20;

    public static ChannelClass of(String clubId, String label, String vehicleClass) {
        return of(clubId, label, vehicleClass, null, null);
    }

    public static ChannelClass of(String clubId, String label, String vehicleClass, String championshipId, String eventId) {
        String tag = label != null && !label.isBlank() ? label.strip() : vehicleClass;
        if (tag == null) {
            tag = clubId;
        }
        if (tag.length() > MAX_TAG_LENGTH) {
            tag = tag.substring(0, MAX_TAG_LENGTH - 1) + "…";
        }
        return new ChannelClass(clubId, tag, vehicleClass, championshipId, eventId);
    }
}
