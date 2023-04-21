import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import {AppRoutingModule} from "./app-routing.module";
import {AuthConfig, OAuthModule} from "angular-oauth2-oidc";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {Oauth2Interceptor} from "./fourleft/private/services/security/oauth2-interceptor.service";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";


const authConfig: AuthConfig = {
  issuer: 'https://heimdall.busata.io/realms/FourLeft',
  redirectUri: window.location.origin + "/private",
  clientId: 'FourLeft-Frontend',
  scope: 'openid profile email offline_access',
  responseType: 'code',
  // at_hash is not present in JWT token
  disableAtHashCheck: true,
  showDebugInformation: true
}
@NgModule({
  imports: [
    HttpClientModule,
    OAuthModule.forRoot(),
    AppRoutingModule,
    BrowserModule,
    BrowserAnimationsModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: Oauth2Interceptor,
      multi: true
    },
    { provide: AuthConfig, useValue: authConfig },
    ],
  bootstrap: [AppComponent],
  declarations: [
  ]
})
export class AppModule { }
