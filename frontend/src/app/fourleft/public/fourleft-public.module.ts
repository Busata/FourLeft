import { NgModule } from '@angular/core';
import {FourleftPublicRoutingModule} from "./fourleft-public-routing.module";
import { FourleftPublicContainerComponent } from './fourleft-public-container/fourleft-public-container.component';
import { UserCommunityProgressComponent } from './user-community-progress/user-community-progress.component';
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {MatButtonModule} from "@angular/material/button";
import {NgIf, NgOptimizedImage} from "@angular/common";
import {ClipboardModule} from "@angular/cdk/clipboard";
import {MatIconModule} from "@angular/material/icon";
import {MatSnackBarModule} from "@angular/material/snack-bar";

@NgModule({
  declarations: [
    FourleftPublicContainerComponent,
    UserCommunityProgressComponent
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
        NgOptimizedImage
    ],
  providers:[]
})
export class FourleftPublicModule { }
