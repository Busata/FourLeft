import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { UsersListComponent } from './fourleft/admin/users/users-list/users-list.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {UsersService} from "./fourleft/admin/users/users.service";
import { FieldMappingsListComponent } from './fourleft/admin/field-mappings/field-mappings-list/field-mappings-list.component';
import {FieldMappingsService} from "./fourleft/admin/field-mappings/field-mappings.service";
import { FieldMappingEditComponent } from './fourleft/admin/field-mappings/field-mapping-edit/field-mapping-edit.component';
import { ClubMergeResultsComponent } from './fourleft/admin/club-merger/club-merge-results/club-merge-results.component';
import {ClubMergerService} from "./fourleft/admin/club-merger/club-merger.service";
import { ClubTiersContainerComponent } from './fourleft/admin/club-tiers/club-tiers-container/club-tiers-container.component';
import {ClubTiersService} from './fourleft/admin/club-tiers/club-tiers.service';
import {FieldMappingQueryService} from './fourleft/admin/field-mappings/field-mapping-query.service';
import {MatCardModule} from '@angular/material/card';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import { TierConfigurationComponent } from './fourleft/admin/club-tiers/tier-configuration/tier-configuration.component';
import {MatIconModule} from '@angular/material/icon';
import {DragDropModule} from '@angular/cdk/drag-drop';
import { RacenetNamesComponent } from './fourleft/admin/club-tiers/racenet-names/racenet-names.component';
import { ClubEventConfigurationComponent } from './fourleft/admin/club-tiers/club-event-configuration/club-event-configuration.component';
import { TierVehicleConfigurationComponent } from './fourleft/admin/club-tiers/tier-vehicle-configuration/tier-vehicle-configuration.component';
import {AuthConfig, OAuthModule} from 'angular-oauth2-oidc';
import {Oauth2Interceptor} from './oauth2-interceptor.service';

const authConfig: AuthConfig = {
  issuer: 'https://heimdall.busata.io/realms/FourLeft',
  redirectUri: window.location.origin + "/#/",
  clientId: 'FourLeft-Frontend',
  scope: 'openid profile email offline_access',
  responseType: 'code',
  // at_hash is not present in JWT token
  disableAtHashCheck: true,
  showDebugInformation: true
}

@NgModule({
  declarations: [
    AppComponent,
    UsersListComponent,
    FieldMappingsListComponent,
    FieldMappingEditComponent,
    ClubMergeResultsComponent,
    ClubTiersContainerComponent,
    TierConfigurationComponent,
    RacenetNamesComponent,
    ClubEventConfigurationComponent,
    TierVehicleConfigurationComponent
  ],
  imports: [
    HttpClientModule,
    BrowserModule,
    OAuthModule.forRoot(),
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatIconModule,
    DragDropModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: Oauth2Interceptor,
      multi: true
    },
    { provide: AuthConfig, useValue: authConfig },
    UsersService, FieldMappingsService, ClubMergerService,ClubTiersService,FieldMappingQueryService],
  bootstrap: [AppComponent]
})
export class AppModule { }
