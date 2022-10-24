import { Component } from '@angular/core';
import {NullValidationHandler, OAuthErrorEvent, OAuthService} from 'angular-oauth2-oidc';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'fourleft-frontend';

  constructor(private oauthService: OAuthService, private router: Router) {
    this.configure();
  }

  public login() {
    this.oauthService.initCodeFlow();
  }

  public isLoggedIn() {
    return this.oauthService.hasValidAccessToken();

  }

  public logoff() {
    this.oauthService.logOut();
  }

  private configure() {
    this.oauthService.events.subscribe(e => e instanceof OAuthErrorEvent ? console.error(e) : console.warn(e));
    this.oauthService.setupAutomaticSilentRefresh();
    this.oauthService.loadDiscoveryDocumentAndTryLogin().then((event) => {
      return this.oauthService.tryLoginCodeFlow().then(() => {
        if(!this.oauthService.hasValidAccessToken()) {

          return this.oauthService.initCodeFlow();
        } else {
          this.router.initialNavigation();

        }
      });
    })

    this.oauthService.tokenValidationHandler = new NullValidationHandler();
  }
}
