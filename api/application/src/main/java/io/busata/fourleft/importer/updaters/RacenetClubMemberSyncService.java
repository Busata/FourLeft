package io.busata.fourleft.importer.updaters;


import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.ClubMember;
import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.gateway.racenet.RacenetGateway;
import io.busata.fourleft.gateway.racenet.dto.club.DR2ClubMember;
import io.busata.fourleft.gateway.racenet.dto.club.DR2ClubMembers;
import io.busata.fourleft.gateway.racenet.factory.ClubMemberFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetClubMemberSyncService {

    private final RacenetGateway client;
    private final ClubMemberFactory clubMemberFactory;

    public void syncWithRacenet(Club club) {
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
