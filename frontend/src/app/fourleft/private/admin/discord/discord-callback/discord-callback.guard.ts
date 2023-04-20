import {inject} from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn, Router,
  RouterStateSnapshot
} from "@angular/router";
import {DiscordIntegrationApiService} from "../discord-integration-api.service";
import {map} from "rxjs";

export const discordCallbackGuard : CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  let discordIntegrationService = inject(DiscordIntegrationApiService);
  const token = <string> route.queryParamMap.get('code');
  return discordIntegrationService.postDiscordToken(token).pipe(map(() => {
    window?.top?.close();
    return true;
  }));
}
