import {Component, Input} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {SingleClubViewForm} from '../single-club-view-form/single-club-view.form';
import {ConcatenationViewForm} from './concatenation-view.form';

@Component({
  selector: 'app-concatenation-view-form',
  templateUrl: './concatenation-view-form.component.html',
  styleUrls: ['./concatenation-view-form.component.scss']
})
export class ConcatenationViewFormComponent {

  @Input("formGroup")
  form!: ConcatenationViewForm

  cast(form: AbstractControl) {
    return form as SingleClubViewForm;
  }
}
