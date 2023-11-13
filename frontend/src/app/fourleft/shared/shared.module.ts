import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FieldMapComponent} from './field-map/field-map.component';
import {NgIconComponent} from '@ng-icons/core';
import {PlayerSearchComponent} from "./player-search/player-search.component";
import {PhotoStreamComponent} from "./photo-stream/photo-stream.component";
import { PhotoDataEditor } from './photo-stream/photo-data-editor/photo-data-editor.component';
import { SlideToggleComponent } from './slide-toggle/slide-toggle.component';



@NgModule({
  declarations: [FieldMapComponent, PlayerSearchComponent, PhotoStreamComponent, PhotoDataEditor, SlideToggleComponent],
  imports: [
    CommonModule,
    NgIconComponent
  ],
  exports: [FieldMapComponent, PlayerSearchComponent, PhotoStreamComponent, PhotoDataEditor, SlideToggleComponent]
})
export class SharedModule { }
