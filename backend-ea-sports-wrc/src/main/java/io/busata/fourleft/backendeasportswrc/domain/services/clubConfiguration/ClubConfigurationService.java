package io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubConfigurationService {

    private final ClubConfigurationRepository clubConfigurationRepository;



    public List<ClubConfiguration> findSyncableClubs() {

        return clubConfigurationRepository.findAll()
                .stream().filter(ClubConfiguration::isKeepSynced)
                .toList();

    }

    @Transactional
    public void setClubSync(String clubId, boolean keepSynced) {
        List<ClubConfiguration> clubConfigurations = this.clubConfigurationRepository.findByClubId(clubId).stream().map(clubConfiguration -> {
            clubConfiguration.setKeepSynced(keepSynced);
            return clubConfiguration;
        }).collect(Collectors.toList());

        this.clubConfigurationRepository.saveAll(clubConfigurations);
    }

    @Transactional
    public void addClubSync(String clubId) {
        if(clubConfigurationRepository.findByClubId(clubId).isEmpty()) {
            this.clubConfigurationRepository.save(new ClubConfiguration(clubId));
        }
    }
}
