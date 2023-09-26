package io.busata.fourleft.application.dirtrally2.importer.clubs;


import io.busata.fourleft.infrastructure.clients.racenet.RacenetGateway;
import io.busata.fourleft.application.dirtrally2.importer.factory.ClubMemberFactory;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubMember;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubMember;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubMembers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
class RacenetClubMemberSyncService {

    private final RacenetGateway client;
    private final ClubMemberFactory clubMemberFactory;

    public void syncWithRacenet(Club club) {
        if(club.getMembers() > 1500) {
            log.warn("Warning, club too large to sync members for. Club: {}", club.getReferenceId());
            return;
        }

        List<ClubMember> members = getAllMembers(club).stream().map(clubMemberFactory::create).map(member -> {
            member.setClub(club);
            return member;
        }).toList();

        club.updateMembers(members);

    }

    private List<DR2ClubMember> getAllMembers(Club club) {
        List<DR2ClubMember> members = new ArrayList<>();

        var allMembersFetched = false;
        var currentPage = 1;
        while (!allMembersFetched) {

            DR2ClubMembers clubMembers = client.getClubMembers(club.getReferenceId(), 200, currentPage);

            if(clubMembers.members() != null) {
                if(clubMembers.members().Owner() != null) {
                    members.addAll(clubMembers.members().Owner());
                }
                if(clubMembers.members().Player() != null) {
                    members.addAll(clubMembers.members().Player());
                }
                if(clubMembers.members().Administrator() != null) {
                    members.addAll(clubMembers.members().Administrator());
                }
            }

            if (clubMembers.pageCount() == currentPage) {
                allMembersFetched = true;
            } else {
                currentPage += 1;
            }
        }

        return members;
    }
}
