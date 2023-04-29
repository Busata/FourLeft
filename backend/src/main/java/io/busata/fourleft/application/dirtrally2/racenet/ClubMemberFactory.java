package io.busata.fourleft.application.dirtrally2.racenet;

import io.busata.fourleft.api.models.ClubMemberTo;
import io.busata.fourleft.domain.dirtrally2.clubs.models.ClubMember;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubMemberFactory {

    public ClubMember create(DR2ClubMember dr2ClubMember) {
        ClubMember clubMember = new ClubMember();

        clubMember.setReferenceId(dr2ClubMember.id());

        clubMember.setDisplayName(dr2ClubMember.displayName());
        clubMember.setMembershipType(dr2ClubMember.membershipType());

        clubMember.setChampionshipParticipation(dr2ClubMember.championshipParticipation());

        clubMember.setChampionshipGolds(dr2ClubMember.championshipGolds());
        clubMember.setChampionshipSilvers(dr2ClubMember.championshipSilvers());
        clubMember.setChampionshipBronzes(dr2ClubMember.championshipBronzes());

        return clubMember;
    }
    public ClubMemberTo create(ClubMember member) {
        return new ClubMemberTo(
                member.getDisplayName(),
                member.getMembershipType(),
                member.getChampionshipGolds(),
                member.getChampionshipSilvers(),
                member.getChampionshipBronzes(),
                member.getChampionshipParticipation()
        );
    }
}
