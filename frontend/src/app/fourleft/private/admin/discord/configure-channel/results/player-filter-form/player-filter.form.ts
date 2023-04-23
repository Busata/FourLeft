import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {PlayerFilterTo} from '@server-models';


export class PlayerFilterForm extends FormGroup {
  public readonly playerFilterType = this.get('playerFilterType') as FormControl;
  public readonly racenetNames = this.get('racenetNames') as FormArray;

  constructor(value?: PlayerFilterTo) {
    super({
      playerFilterType: new FormControl(value?.playerFilterType || 'NONE', {}),
      racenetNames: new FormArray(value?.racenetNames?.map(name => new FormControl(name, {})) || [])
    });
  }

  addPlayer(name: string) {
    this.racenetNames.push(new FormControl(name, {}))
  }

  removePlayer(i: number) {
    this.racenetNames.removeAt(i);
  }
}
