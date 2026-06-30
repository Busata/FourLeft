import { NgModule } from '@angular/core';
import {FourleftPublicRoutingModule} from "./fourleft-public-routing.module";
import { FourleftPublicContainerComponent } from './fourleft-public-container/fourleft-public-container.component';
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {MatButtonModule} from "@angular/material/button";
import {DatePipe, NgForOf, NgIf, NgOptimizedImage} from "@angular/common";
import {ClipboardModule} from "@angular/cdk/clipboard";
import {MatIconModule} from "@angular/material/icon";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import { HomeComponent } from './home/home.component';
import {SharedModule} from '../shared/shared.module';
import {NgIconComponent} from '@ng-icons/core';
import {
  EASportsWRCProfileRedirectComponent
} from "./easportswrc/profile/easports-wrc-profile-redirect/easportswrc-profile-redirect.component";
import {EASportsWRCPageComponent} from "./easportswrc/easports-wrcpage/easports-wrcpage.component";
import { EASportsWRCProfileContainerComponent } from './easportswrc/profile/easports-wrc-profile-container/easports-wrcprofile-container.component';
import { EASportsWRCProfileFormComponent } from './easportswrc/profile/easports-wrcprofile-form/easports-wrcprofile-form.component';
import { PrivacyPolicyComponent } from './legal/privacy-policy.component';
import { TermsOfServiceComponent } from './legal/terms-of-service.component';

@NgModule({
  declarations: [
    FourleftPublicContainerComponent,
    HomeComponent,
    EASportsWRCPageComponent,
    EASportsWRCProfileRedirectComponent,
    EASportsWRCProfileContainerComponent,
    EASportsWRCProfileFormComponent,
    PrivacyPolicyComponent,
    TermsOfServiceComponent
  ],
  imports: [
    FourleftPublicRoutingModule,
    MatCardModule,
    MatInputModule,
    FormsModule,
    MatCheckboxModule,
    MatSlideToggleModule,
    MatButtonModule,
    NgIf,
    MatSnackBarModule,
    ClipboardModule,
    MatIconModule,
    NgOptimizedImage,
    NgForOf,
    SharedModule,
    NgIconComponent,
    DatePipe,
    ReactiveFormsModule,
  ],
  providers:[]
})
export class FourleftPublicModule { }
