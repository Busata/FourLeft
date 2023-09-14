import {Form, FormArray, FormControl, FormGroup} from "@angular/forms";
import {AliasUpdateDataTo} from "@server-models";


export class AliasForm extends FormGroup {
  readonly id = this.get('id') as FormControl
  readonly displayName = this.get('displayName') as FormControl;
  readonly platform = this.get('platform') as FormControl;
  readonly controller = this.get('controller') as FormControl;
  readonly racenet = this.get('racenet') as FormControl;
  readonly aliases = this.get('aliases') as FormArray;


  constructor(data?: AliasUpdateDataTo) {
    super({
      id: new FormControl(data?.id, {}),
      displayName: new FormControl(data?.displayName, {}),
      platform: new FormControl(data?.platform, {}),
      controller: new FormControl(data?.controller, {}),
      racenet: new FormControl(data?.racenet, {}),
      aliases: new FormArray(data?.aliases?.map(alias => new FormControl(alias, {})) || []),
    });

    this.racenet.disable();

  }


  addAlias(alias: string) {
    this.aliases.controls.push(new FormControl(alias, {}))
  }

  removeAliasAt(idx: number) {
    this.aliases.removeAt(idx);
  }
}
