import {Form, FormArray, FormControl, FormGroup} from "@angular/forms";


export class ProfileForm extends FormGroup {
  readonly id = this.get('id') as FormControl
  readonly displayName = this.get('displayName') as FormControl;
  readonly platform = this.get('platform') as FormControl;
  readonly controller = this.get('controller') as FormControl;
  readonly peripheral = this.get('peripheral') as FormControl;
  readonly racenet = this.get('racenet') as FormControl;
  readonly trackDiscord = this.get('trackDiscord') as FormControl;


  constructor(data?: any) {
    super({
      id: new FormControl(data?.id, {}),
      displayName: new FormControl(data?.displayName, {}),
      platform: new FormControl(data?.platform, {}),
      controller: new FormControl(data?.controller, {}),
      peripheral: new FormControl(data?.peripheral, {}),
      racenet: new FormControl(data?.racenet, {}),
      trackDiscord: new FormControl(data?.trackDiscord, {}),
    });

    this.racenet.disable();

  }
}
