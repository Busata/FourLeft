import {inject} from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn, Router,
  RouterStateSnapshot
} from "@angular/router";
import {DiscordIntegrationApiService} from "../discord-integration-api.service";
import {map, of} from "rxjs";

export const discordAuthenticationGuard : CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  let discordIntegrationService = inject(DiscordIntegrationApiService);
  let router = inject(Router);


  if(state.url.startsWith("/private/discord/authenticate") || state.url.startsWith("/private/discord/callback")) {
    return of(true);
  }


  return discordIntegrationService.isAuthenticated().pipe(map((status: any) => {
    if(!status.authenticated) {
      router.navigate(["/private/discord/authenticate"]);
      return true;
    }
    return status.authenticated;
  }));
}
