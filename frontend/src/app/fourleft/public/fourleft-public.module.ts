import { NgModule } from '@angular/core';
import {FourleftPublicRoutingModule} from "./fourleft-public-routing.module";
import { FourleftPublicContainerComponent } from './fourleft-public-container/fourleft-public-container.component';
import { UserCommunityProgressComponent } from './user-community-progress/user-community-progress.component';
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
import { ResultsContainerComponent } from './results/results-container/results-container.component';
import { HomeComponent } from './home/home.component';
import { ResultDetailsComponent } from './results/result-details/result-details.component';
import {SharedModule} from '../shared/shared.module';
import { ControllerTypeComponent } from './results/controller-type/controller-type.component';
import {NgIconComponent} from '@ng-icons/core';
import {PlatformTypeComponent} from './results/platform-type/platform-type.component';
import { AliasContainerComponent } from './alias/alias-container/alias-container.component';
import { AliasFormComponent } from './alias/alias-form/alias-form.component';
import {DiscordModule} from "../private/admin/discord/discord.module";
import { AliasPageComponent } from './alias/alias-page/alias-page.component';
import { AliasRedirectComponent } from './alias/alias-redirect/alias-redirect.component';
import {PublicGalleryContainerComponent} from "./gallery/public-gallery/public-gallery-container.component";
import {InfiniteScrollModule} from "ngx-infinite-scroll";
import { GalleryPhotoLightBoxComponent } from './gallery/gallery-photo-light-box/gallery-photo-light-box.component';

@NgModule({
  declarations: [
    FourleftPublicContainerComponent,
    UserCommunityProgressComponent,
    ResultsContainerComponent,
    HomeComponent,
    ResultDetailsComponent,
    ControllerTypeComponent,
    PlatformTypeComponent,
    AliasContainerComponent,
    AliasFormComponent,
    AliasPageComponent,
    AliasRedirectComponent,
    PublicGalleryContainerComponent,
    GalleryPhotoLightBoxComponent
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
    DiscordModule,
    ReactiveFormsModule,
    InfiniteScrollModule,
  ],
  providers:[]
})
export class FourleftPublicModule { }
