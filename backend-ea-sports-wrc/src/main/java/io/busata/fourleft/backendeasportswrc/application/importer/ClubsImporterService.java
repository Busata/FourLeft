package io.busata.fourleft.backendeasportswrc.application.importer;

import io.busata.fourleft.backendeasportswrc.domain.ClubConfigurationRepository;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.RacenetGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ClubsImporterService {

    private final RacenetGateway racenetGateway;

    private final ClubConfigurationRepository clubConfigurationRepository;

    public void sync() {



    }


    public void update(String clubId) {
        racenetGateway.getClubDetail(clubId).thenCompose(clubDetailsTo -> {

            



        });

    }

}
