import { NgModule } from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {NgIconComponent} from '@ng-icons/core';
import {SharedModule} from "../../../shared/shared.module";
import {GalleryPageComponent} from "./gallery-page/gallery-page.component";
import {GalleryRoutingModule} from "./gallery-routing.module";
import { GalleryHomeComponent } from './gallery-home/gallery-home.component';
import { GalleryUploadButtonComponent } from './gallery-upload-button/gallery-upload-button.component';

@NgModule({
  declarations: [
    GalleryPageComponent,
    GalleryHomeComponent,
    GalleryUploadButtonComponent
  ],
  providers: [],
  exports: [],
  imports: [
    SharedModule,
    GalleryRoutingModule,
    CommonModule,
    MatButtonModule,
    MatCardModule,
    ReactiveFormsModule,
    NgOptimizedImage,
    FormsModule,
    MatSnackBarModule,
    NgIconComponent
  ]
})
export class GalleryModule { }
