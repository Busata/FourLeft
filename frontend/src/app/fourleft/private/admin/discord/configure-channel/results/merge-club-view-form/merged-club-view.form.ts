import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {SingleClubViewForm} from "../single-club-view-form/single-club-view.form";
import {MergedViewTo} from "@server-models";
import {RacenetFilterForm} from '../racenet-filter-form/racenet-filter.form';

export class MergedClubViewForm extends FormGroup {

  public readonly type = this.get('type') as FormControl;
  public readonly name = this.get('name') as FormControl;
  public readonly resultViews = this.get('resultViews') as FormArray;
  public readonly racenetFilter = this.get('racenetFilter') as RacenetFilterForm;

  constructor(value?: MergedViewTo) {
    super({
        type: new FormControl('mergeClub', {}),
        name: new FormControl(value?.name, {}),
      resultViews: new FormArray(value?.resultViews?.map(v => new SingleClubViewForm(v)) || []),
      racenetFilter: new RacenetFilterForm(value?.racenetFilter)
    })
  }


  addSingleClubView() {
    this.resultViews.push(new SingleClubViewForm());
  }

  removeClubAt(i: number) {
    this.resultViews.removeAt(i);
  }
}
