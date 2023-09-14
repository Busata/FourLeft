import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FieldMapComponent} from './field-map/field-map.component';
import {NgIconComponent} from '@ng-icons/core';
import {PlayerSearchComponent} from "./player-search/player-search.component";



@NgModule({
  declarations: [FieldMapComponent, PlayerSearchComponent],
  imports: [
    CommonModule,
    NgIconComponent
  ],
  exports: [FieldMapComponent, PlayerSearchComponent]
})
export class SharedModule { }
