package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.CurrentChampionshipTo;

import java.util.List;

public record ClubDetailsTo(
        String clubId,
        String officialClubType,
        String accessCode,
        Long status,
        Long role,
        Long reaction,
        String creationSSID,
        String creatorDisplayName,
        String ownerDisplayName,
        String ownerProfileImageUrl,
        String clubName,
        String clubDescription,
        Long activeMemberCount,
        Long likeCount,
        Long dislikeCount,
        String imageCatalogueID,
        Long platform,
        Long accessLevel,
        String clubCreatedAt,
        List<SocialMediaLinkTo> socialMediaLinks,
        List<String> championshipIDs,
        CurrentChampionshipTo currentChampionship

) {

}
