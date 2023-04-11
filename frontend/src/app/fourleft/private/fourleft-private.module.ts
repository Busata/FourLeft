import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AppComponent} from "../../app.component";
import {UsersListComponent} from "./admin/users/users-list/users-list.component";
import {FieldMappingsListComponent} from "./admin/field-mappings/field-mappings-list/field-mappings-list.component";
import {FieldMappingEditComponent} from "./admin/field-mappings/field-mapping-edit/field-mapping-edit.component";
import {FourleftPrivateContainerComponent} from "./fourleft-private-container/fourleft-private-container.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {DiscordModule} from "./admin/discord/discord.module";
import {UsersService} from "./admin/users/users.service";
import {FieldMappingsService} from "./admin/field-mappings/field-mappings.service";
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
    HasPermissionDirective,
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
    UsersService, FieldMappingsService, FieldMappingQueryService]
})
export class FourleftPrivateModule {
}
