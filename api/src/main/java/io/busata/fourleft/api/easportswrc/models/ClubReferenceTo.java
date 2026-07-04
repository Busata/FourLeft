package io.busata.fourleft.api.easportswrc.models;

/** Lightweight club identity for pickers/dropdowns: the id to fetch its summary + a display name. */
public record ClubReferenceTo(String clubId, String clubName) {
}
