import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FieldMapComponent} from './field-map/field-map.component';
import {NgIconComponent} from '@ng-icons/core';



@NgModule({
  declarations: [FieldMapComponent],
  imports: [
    CommonModule,
    NgIconComponent
  ],
  exports: [FieldMapComponent]
})
export class SharedModule { }
