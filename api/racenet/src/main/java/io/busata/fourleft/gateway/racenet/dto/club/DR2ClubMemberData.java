package io.busata.fourleft.gateway.racenet.dto.club;


import java.util.List;

public record DR2ClubMemberData(
        List<DR2ClubMember> Owner,
        List<DR2ClubMember> Administrator,
        List<DR2ClubMember> Player
) {
}
