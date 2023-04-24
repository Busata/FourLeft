import {Component, Input} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {SingleClubViewForm} from '../single-club-view-form/single-club-view.form';
import {ConcatenationViewForm} from './concatenation-view.form';
import {RacenetFilterForm} from '../racenet-filter-form/racenet-filter.form';

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

  promotePlayer(idx: number, playerIdx: number) {
    const sourcePartitionElement = (this.form.resultViews.controls[idx] as SingleClubViewForm).racenetFilter;
    const targetPartitionElement = (this.form.resultViews.controls[idx - 1] as SingleClubViewForm).racenetFilter;

    targetPartitionElement.racenetNames.push(sourcePartitionElement.racenetNames.controls[playerIdx]);
    sourcePartitionElement.racenetNames.removeAt(playerIdx);
  }

  demotePlayer(idx: number, playerIdx: number) {
    const sourcePartitionElement = (this.form.resultViews.controls[idx] as SingleClubViewForm).racenetFilter;
    const targetPartitionElement = (this.form.resultViews.controls[idx + 1] as SingleClubViewForm).racenetFilter;
    targetPartitionElement.racenetNames.push(sourcePartitionElement.racenetNames.controls[playerIdx]);
    sourcePartitionElement.racenetNames.removeAt(playerIdx);
  }
}
