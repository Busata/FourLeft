import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AppComponent} from "../../app.component";
import {FourleftPrivateContainerComponent} from "./fourleft-private-container/fourleft-private-container.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {FourleftPrivateRoutingModule} from "./fourleft-private-routing.module";
import {LoginService} from "./services/security/login.service";
import {AuthenticationService} from "./services/security/authentication.service";
import {UserStoreService} from "./services/security/user-store.service";
import {HasPermissionDirective} from "./services/security/has-permission.directive";
import { ManagementContainerComponent } from './admin/management/management-container/management-container.component';


@NgModule({
  declarations: [AppComponent,
    HasPermissionDirective,
    FourleftPrivateContainerComponent,
    ManagementContainerComponent],
  imports: [
    FourleftPrivateRoutingModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    DragDropModule
  ],
  providers: [
    LoginService, AuthenticationService, UserStoreService]
})
export class FourleftPrivateModule {
}
