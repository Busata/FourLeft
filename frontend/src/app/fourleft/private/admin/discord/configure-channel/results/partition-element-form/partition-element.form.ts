import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {PartitionElementTo} from "@server-models";

export class PartitionElementForm extends FormGroup {

  readonly name = this.get('name') as FormControl;
  readonly racenetNames = this.get('racenetNames') as FormArray;

  constructor(value?: PartitionElementTo) {
    super({
      name: new FormControl(value?.name, {}),
      order: new FormControl(value?.order, {}),
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
