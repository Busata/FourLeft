package io.busata.fourleft.endpoints.dirtrally2;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.AliasRequestResultTo;
import io.busata.fourleft.api.models.AliasUpdateDataTo;
import io.busata.fourleft.api.models.AliasUpdateRequestTo;
import io.busata.fourleft.application.dirtrally2.alias.AliasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AliasEndpoint {

    private final AliasService aliasService;


    @RequestMapping(method = RequestMethod.POST, value=RoutesTo.REQUEST_ALIAS_UPDATE)
    public AliasRequestResultTo requestAliasUpdate(@RequestBody AliasUpdateRequestTo request) {
        return aliasService.requestUpdate(request.discordId(), request.racenet()).map(uuid -> {
            return new AliasRequestResultTo(true, uuid);
        }).orElse(new AliasRequestResultTo(false, null));
    }

    @RequestMapping(method = RequestMethod.GET, value=RoutesTo.REQUEST_ALIAS_GET)
    public AliasUpdateDataTo getPlayerInfo(@PathVariable UUID requestId) {
        return aliasService.getPlayerInfo(requestId).map( playerInfo -> {
               return new AliasUpdateDataTo(
                       playerInfo.getId(),
                       playerInfo.getDisplayName(),
                       playerInfo.getController(),
                       playerInfo.getPlatform(),
                     playerInfo.getRacenet(),
                       playerInfo.isTrackCommunity(),
                     playerInfo.getAliases().stream().toList());
        }).orElseThrow();
    }

    @RequestMapping(method = RequestMethod.POST, value=RoutesTo.REQUEST_ALIAS_GET)
    public AliasUpdateDataTo updatePlayerInfo(@PathVariable UUID requestId, @RequestBody AliasUpdateDataTo data) {

        var playerInfo = aliasService.updatePlayerInfo(requestId, data);
        return new AliasUpdateDataTo(
                playerInfo.getId(),
                playerInfo.getDisplayName(),
                playerInfo.getController(),
                playerInfo.getPlatform(),
                playerInfo.getRacenet(),
                playerInfo.isTrackCommunity(),
                playerInfo.getAliases().stream().toList());
    }

}
