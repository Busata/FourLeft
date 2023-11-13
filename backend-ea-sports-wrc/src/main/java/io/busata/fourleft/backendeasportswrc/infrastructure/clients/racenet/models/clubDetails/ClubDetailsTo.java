package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder()
public record ClubDetailsTo(
        String clubID,
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
        Optional<ChampionshipTo> currentChampionship

) {

}
