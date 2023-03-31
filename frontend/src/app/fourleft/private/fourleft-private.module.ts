import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
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
import {FourleftPrivateContainerComponent} from "./fourleft-private-container/fourleft-private-container.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {DiscordModule} from "./admin/discord/discord.module";
import {UsersService} from "./admin/users/users.service";
import {FieldMappingsService} from "./admin/field-mappings/field-mappings.service";
import {ClubMergerService} from "./admin/club-merger/club-merger.service";
import {ClubTiersService} from "./admin/club-tiers/club-tiers.service";
import {FieldMappingQueryService} from "./admin/field-mappings/field-mapping-query.service";
import {FourleftPrivateRoutingModule} from "./fourleft-private-routing.module";
import {LoginService} from "./services/security/login.service";
import {AuthenticationService} from "./services/security/authentication.service";
import {UserStoreService} from "./services/security/user-store.service";
import {HasPermissionDirective} from "./services/security/has-permission.directive";


@NgModule({
  declarations: [AppComponent,
    UsersListComponent,
    FieldMappingsListComponent,
    FieldMappingEditComponent,
    ClubMergeResultsComponent,
    HasPermissionDirective,
    ClubTiersContainerComponent,
    TierConfigurationComponent,
    RacenetNamesComponent,
    ClubEventConfigurationComponent,
    TierVehicleConfigurationComponent,
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
    DiscordModule
  ],
  providers: [
    LoginService, AuthenticationService, UserStoreService,
    UsersService, FieldMappingsService, ClubMergerService, ClubTiersService, FieldMappingQueryService]
})
export class FourleftPrivateModule {
}
