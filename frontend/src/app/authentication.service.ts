import { Injectable } from '@angular/core';
import {NullValidationHandler, OAuthErrorEvent, OAuthService} from "angular-oauth2-oidc";
import {Router} from "@angular/router";
import {Location} from "@angular/common";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private readonly targetUrl: string;

  constructor(private oauthService: OAuthService, private router: Router, private location: Location) {
    this.targetUrl = this.location.path(true);
  }

  public configure() {
      this.oauthService.events.subscribe(e => e instanceof OAuthErrorEvent ? console.error(e) : console.warn(e));
      this.oauthService.setupAutomaticSilentRefresh();
      this.oauthService.loadDiscoveryDocumentAndTryLogin().then((event) => {

        if(!this.oauthService.hasValidAccessToken()) {
          return this.oauthService.initCodeFlow(this.targetUrl);
        } else {
          this.router.initialNavigation();
          if(this.oauthService.state) {
            this.router.navigateByUrl(decodeURIComponent(<string>this.oauthService.state));
          } else if(this.targetUrl) {
            this.router.navigateByUrl(this.targetUrl);
          } else {
            this.router.navigateByUrl("/");

          }

        }
      })

      this.oauthService.tokenValidationHandler = new NullValidationHandler();
    }
}
