import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {AppComponent} from "../../app.component";
import {UsersListComponent} from "./admin/users/users-list/users-list.component";
import {FieldMappingsListComponent} from "./admin/field-mappings/field-mappings-list/field-mappings-list.component";
import {FieldMappingEditComponent} from "./admin/field-mappings/field-mapping-edit/field-mapping-edit.component";
import {ClubMergeResultsComponent} from "./admin/club-merger/club-merge-results/club-merge-results.component";
import {ClubTiersContainerComponent} from "./admin/club-tiers/club-tiers-container/club-tiers-container.component";
import {TierConfigurationComponent} from "./admin/club-tiers/tier-configuration/tier-configuration.component";
import {RacenetNamesComponent} from "./admin/club-tiers/racenet-names/racenet-names.component";
import {
  ClubEventConfigurationComponent
} from "./admin/club-tiers/club-event-configuration/club-event-configuration.component";
import {
  TierVehicleConfigurationComponent
} from "./admin/club-tiers/tier-vehicle-configuration/tier-vehicle-configuration.component";
import {
  DiscordIntegrationContainerComponent
} from "./admin/discord-integration/discord-integration-container/discord-integration-container.component";
import {DiscordCallbackComponent} from "./admin/discord-integration/discord-callback/discord-callback.component";
import {FourleftPrivateContainerComponent} from "./fourleft-private-container/fourleft-private-container.component";
import {HTTP_INTERCEPTORS} from "@angular/common/http";
import {AuthConfig} from "angular-oauth2-oidc";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {DiscordIntegrationModule} from "./admin/discord-integration/discord-integration.module";
import {Oauth2Interceptor} from "./services/oauth2-interceptor.service";
import {UsersService} from "./admin/users/users.service";
import {FieldMappingsService} from "./admin/field-mappings/field-mappings.service";
import {ClubMergerService} from "./admin/club-merger/club-merger.service";
import {ClubTiersService} from "./admin/club-tiers/club-tiers.service";
import {FieldMappingQueryService} from "./admin/field-mappings/field-mapping-query.service";
import {DiscordIntegrationApiService} from "./admin/discord-integration/discord-integration-api.service";
import {FourleftPrivateRoutingModule} from "./fourleft-private-routing.module";
import {AuthenticationService} from "./services/authentication.service";


@NgModule({
  declarations: [AppComponent,
    UsersListComponent,
    FieldMappingsListComponent,
    FieldMappingEditComponent,
    ClubMergeResultsComponent,
    ClubTiersContainerComponent,
    TierConfigurationComponent,
    RacenetNamesComponent,
    ClubEventConfigurationComponent,
    TierVehicleConfigurationComponent,
    DiscordIntegrationContainerComponent,
    DiscordCallbackComponent,
    FourleftPrivateContainerComponent],
  imports: [
    FourleftPrivateRoutingModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    DragDropModule,
    DiscordIntegrationModule
  ],
  providers:[
    AuthenticationService,
    UsersService, FieldMappingsService, ClubMergerService,ClubTiersService,FieldMappingQueryService,
    DiscordIntegrationApiService]
})
export class FourleftPrivateModule { }
