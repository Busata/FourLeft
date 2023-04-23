import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {SingleClubViewForm} from "../single-club-view-form/single-club-view.form";
import {MergedViewTo} from "../../../../../../../common/generated/server-models";

export class MergedClubViewForm extends FormGroup {

  public readonly type = this.get('type') as FormControl;
  public readonly name = this.get('name') as FormControl;
  public readonly resultViews = this.get('resultViews') as FormArray;
  public readonly playerFilter = this.get('playerFilter') as FormGroup;

  constructor(value?: MergedViewTo) {
    super({
        type: new FormControl('mergeClub', {}),
        name: new FormControl(value?.name, {}),
      resultViews: new FormArray(value?.resultViews?.map(v => new SingleClubViewForm(v)) || []),
      playerFilter: new FormGroup({
        playerFilterType: new FormControl(value?.playerFilter?.playerFilterType, {}),
        racenetNames: new FormArray(value?.playerFilter?.racenetNames?.map(name => new FormControl(name, {})) || [])
      })
    })
  }


  addSingleClubView() {
    this.resultViews.push(new SingleClubViewForm());
  }

  removeClubAt(i: number) {
    this.resultViews.removeAt(i);
  }
}
