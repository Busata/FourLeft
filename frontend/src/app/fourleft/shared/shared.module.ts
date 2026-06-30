import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {NgIconComponent} from '@ng-icons/core';
import { SlideToggleComponent } from './slide-toggle/slide-toggle.component';



@NgModule({
  declarations: [SlideToggleComponent],
  imports: [
    CommonModule,
    NgIconComponent
  ],
  exports: [SlideToggleComponent]
})
export class SharedModule { }
