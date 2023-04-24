import {FormControl, FormGroup, Validators} from "@angular/forms";
import {SingleClubViewTo} from "@server-models";
import {RacenetFilterForm} from '../racenet-filter-form/racenet-filter.form';

export class SingleClubViewForm extends FormGroup {

  public readonly id = this.get('id') as FormControl;
  public readonly type = this.get('type') as FormControl;
  public readonly name = this.get('name') as FormControl;
  public readonly clubId = this.get('clubId') as FormControl;
  public readonly usePowerstage = this.get('usePowerstage') as FormControl;
  public readonly powerStageIndex = this.get('powerStageIndex') as FormControl;

  public readonly racenetFilter = this.get('racenetFilter') as RacenetFilterForm;

  constructor(value?: SingleClubViewTo) {
    super({
      id: new FormControl(value?.id, {}),
      type: new FormControl("singleClub", {}),
      name: new FormControl(value?.name, {}),
      clubId: new FormControl(value?.clubId, [Validators.required]),
      usePowerstage: new FormControl(value?.usePowerstage, {}),
      powerStageIndex: new FormControl(value?.powerStageIndex, {}),
      racenetFilter: new RacenetFilterForm(value?.racenetFilter)
    });

  }
}
