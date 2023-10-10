import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import {AppRoutingModule} from "./app-routing.module";
import {AuthConfig, OAuthModule, OAuthStorage} from "angular-oauth2-oidc";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {Oauth2Interceptor} from "./fourleft/private/services/security/oauth2-interceptor.service";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import { NgIconsModule } from '@ng-icons/core';
import { tablerSteeringWheel,
  tablerCheck,
    tablerX,
    tablerEyeClosed,
  tablerBookmark,
  tablerTrashFilled,
  tablerBrandXbox,
  tablerBrandSteam,
  tablerPlaystationTriangle,
  tablerPlaystationSquare,
  tablerPlaystationCircle,
  tablerQuestionMark,
  tablerPlaystationX,
  tablerDeviceGamepad,
  tablerKeyboard
} from '@ng-icons/tabler-icons';
import {InfiniteScrollModule} from "ngx-infinite-scroll";

export function storageFactory(): OAuthStorage {
  return localStorage;
}

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
    InfiniteScrollModule,
    NgIconsModule.withIcons({ tablerSteeringWheel,
      tablerBrandXbox,
      tablerX,
      tablerBrandSteam,
      tablerPlaystationTriangle,
      tablerEyeClosed,
      tablerPlaystationSquare,
      tablerCheck,
      tablerTrashFilled,
      tablerPlaystationCircle,
      tablerQuestionMark,
      tablerBookmark,
      tablerPlaystationX,
      tablerDeviceGamepad,
      tablerKeyboard
    }),
    BrowserAnimationsModule,
  ],
  providers: [
    { provide: OAuthStorage, useFactory: storageFactory },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: Oauth2Interceptor,
      multi: true
    },
    {provide: AuthConfig, useValue: authConfig},
  ],
  bootstrap: [AppComponent],
  declarations: [
  ]
})
export class AppModule { }
