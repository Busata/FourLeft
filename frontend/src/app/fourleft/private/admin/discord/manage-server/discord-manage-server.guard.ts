import {inject} from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn, Router,
  RouterStateSnapshot
} from "@angular/router";
import {DiscordIntegrationApiService} from "../discord-integration-api.service";
import {map, of} from "rxjs";

export const discordManageServerGuard : CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  let discordIntegrationService = inject(DiscordIntegrationApiService);

  let guildId = <string> route.paramMap.get('guildId');

  return discordIntegrationService.canManageServer(guildId).pipe(map((body: any) => {
      return body.canManage;
  }));
}
