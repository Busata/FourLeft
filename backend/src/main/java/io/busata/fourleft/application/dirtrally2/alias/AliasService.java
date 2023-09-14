package io.busata.fourleft.application.dirtrally2.alias;

import com.beust.ah.A;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.BadRequestException;
import io.busata.fourleft.api.models.AliasUpdateDataTo;
import io.busata.fourleft.domain.dirtrally2.alias.AliasUpdateLog;
import io.busata.fourleft.domain.dirtrally2.alias.AliasUpdateRequest;
import io.busata.fourleft.domain.dirtrally2.clubs.BoardEntryRepository;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfo;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AliasService {
    private final AliasUpdateRequestRepository aliasUpdateRequestRepository;
    private final AliasUpdateLogRepository aliasUpdateLogRepository;
    private final PlayerInfoRepository playerInfoRepository;
    private final BoardEntryRepository boardEntryRepository;


    public UUID requestUpdate(String discordId, String racenet) {
        AliasUpdateRequest save = aliasUpdateRequestRepository.save(new AliasUpdateRequest(discordId, racenet));

        return save.getId();
   }


    public boolean requestExists(UUID requestId, String racenet) {
        return aliasUpdateRequestRepository.existsByIdAndRequestedAlias(requestId, racenet);
    }

    public Optional<PlayerInfo> getPlayerInfo(UUID requestId) {
        AliasUpdateRequest aliasUpdateRequest = aliasUpdateRequestRepository.findById(requestId).orElseThrow();

        return playerInfoRepository.findByRacenet(aliasUpdateRequest.getRequestedAlias());
    }

    @Transactional
    public PlayerInfo updatePlayerInfo(UUID requestId, AliasUpdateDataTo data) {
        if(!requestExists(requestId, data.racenet())) {
            throw new BadRequestException("Request does not exist or does not match given racenet");
        }

        PlayerInfo existingPlayerInfo = this.playerInfoRepository.getById(data.id());

        recoverRemovedAliases(data, existingPlayerInfo);

        PlayerInfo updatedPlayerInfo = getUpdatedPlayerInfo(data, existingPlayerInfo);


        updatedPlayerInfo.getAliases().forEach(alias -> {
            updateBoardEntries(alias, updatedPlayerInfo);
        });


        try {
            AliasUpdateRequest byId = aliasUpdateRequestRepository.getById(requestId);

            aliasUpdateLogRepository.save(new AliasUpdateLog(byId.getDiscordId(), new ObjectMapper().writeValueAsString(updatedPlayerInfo)));
        } catch(Exception ex) {
            log.error("Something went wrong trying to log request: {}", requestId);
        }


        return updatedPlayerInfo;
    }

    private void updateBoardEntries(String alias, PlayerInfo updatedPlayerInfo) {
        this.playerInfoRepository.findByRacenet(alias).ifPresent(obsoletePlayerInfo -> {
            if(obsoletePlayerInfo.getId() == updatedPlayerInfo.getId()) {
                return;
            }

            recoverOrphanedAliases(obsoletePlayerInfo);


            log.info("Updated player info {} will replace {} ({} - {})", updatedPlayerInfo.getId(), obsoletePlayerInfo.getId(), obsoletePlayerInfo.getDisplayName(), obsoletePlayerInfo.getRacenet());
            this.boardEntryRepository.replaceObsolete(updatedPlayerInfo.getId(), obsoletePlayerInfo.getId());
            this.playerInfoRepository.delete(obsoletePlayerInfo);
        });
    }

    private PlayerInfo getUpdatedPlayerInfo(AliasUpdateDataTo data, PlayerInfo existingPlayerInfo) {
        existingPlayerInfo.setController(data.controller());
        existingPlayerInfo.setPlatform(data.platform());
        existingPlayerInfo.setDisplayName(data.displayName());
        existingPlayerInfo.setSyncedPlatform(true);
        existingPlayerInfo.updateAliases(data.aliases());

        return this.playerInfoRepository.save(existingPlayerInfo);
    }

    private void recoverRemovedAliases(AliasUpdateDataTo data, PlayerInfo existingPlayerInfo) {
        List<PlayerInfo> removedAliasPlayerInfos = existingPlayerInfo.getAliases()
                .stream()
                .filter(alias -> !data.aliases().contains(alias))
                .map(PlayerInfo::new)
                .toList();

        List<PlayerInfo> orphanedPlayerInfos = playerInfoRepository.saveAll(removedAliasPlayerInfos);

        orphanedPlayerInfos.forEach(orphanPlayerInfo -> {
            boardEntryRepository.updatePlayerInfo(orphanPlayerInfo.getRacenet(), orphanPlayerInfo.getId());
        });
    }

    private void recoverOrphanedAliases(PlayerInfo obsoletePlayerInfo) {
        /*
            Recover the aliases of player infos that are "obsolete" as player infos.
            eg.
            if Busata has alias '6nop' but racenet '6nop' had as alias 'boring damo', we need to recover 'boring damo' as a player info.
         */
        List<PlayerInfo> obsoletePlayerInfos = obsoletePlayerInfo
                .getAliases().stream()
                .map(PlayerInfo::new)
                .toList();

        List<PlayerInfo> updatedObsoletePlayerInfos = playerInfoRepository.saveAll(obsoletePlayerInfos);

        updatedObsoletePlayerInfos.forEach(updatedObsoletePlayerInfo -> {
            this.boardEntryRepository.updatePlayerInfo(updatedObsoletePlayerInfo.getRacenet(), updatedObsoletePlayerInfo.getId());
        });
    }

}
