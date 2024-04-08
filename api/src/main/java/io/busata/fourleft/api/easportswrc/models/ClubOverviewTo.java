package io.busata.fourleft.api.easportswrc.models;

public record ClubOverviewTo(String id, String clubName, String clubDescription, java.time.ZonedDateTime clubCreatedAt,
                             Long activeMemberCount, java.time.LocalDateTime lastLeaderboardUpdate,
                             java.time.LocalDateTime lastDetailsUpdate,
                             java.util.List<ClubChampionshipResultTo> championships) {
}
