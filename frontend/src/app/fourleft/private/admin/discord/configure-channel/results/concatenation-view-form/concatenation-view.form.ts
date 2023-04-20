import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ConcatenationViewTo, MergedViewTo} from '../../../../../../../common/generated/server-models';
import {SingleClubViewForm} from '../single-club-view-form/single-club-view.form';

export class ConcatenationViewForm extends FormGroup {

  public readonly type = this.get('type') as FormControl;
  public readonly name = this.get('name') as FormControl;
  public readonly resultViews = this.get('resultViews') as FormArray;

  constructor(value?: ConcatenationViewTo) {
    super({
      type: new FormControl('concatenationClub', {}),
      name: new FormControl(value?.name, {}),
      resultViews: new FormArray(value?.resultViews?.map(v => new SingleClubViewForm(v)) || [])
    })
  }


  addSingleClubView() {
    this.resultViews.push(new SingleClubViewForm());
  }
}
