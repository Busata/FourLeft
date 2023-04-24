import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {RacenetFilterTo} from '@server-models';

export class RacenetFilterForm extends FormGroup {

  readonly name = this.get('name') as FormControl;
  readonly filterMode = this.get('filterMode') as FormControl;
  readonly isFilterEnabled = this.get('enabled') as FormControl;
  readonly racenetNames = this.get('racenetNames') as FormArray;

  constructor(value?: RacenetFilterTo) {
    super({
      id: new FormControl(value?.id, {}),
      filterMode: new FormControl(value?.filterMode || 'NONE', {}),
      name: new FormControl(value?.name || '', {}),
      enabled: new FormControl(value?.enabled || false, {}),
      racenetNames: new FormArray(value?.racenetNames?.map(e => new FormControl(e)) || [])
    });
  }


  public addPlayer(name: string) {
    this.racenetNames.push(new FormControl(name, {}))
  }

  public removePlayer(idx: number) {
    this.racenetNames.removeAt(idx);
  }
}
