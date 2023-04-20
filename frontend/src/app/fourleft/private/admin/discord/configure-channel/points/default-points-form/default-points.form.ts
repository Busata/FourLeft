import {FormControl, FormGroup} from "@angular/forms";

export class DefaultPointsForm extends FormGroup {

  constructor() {
    super({
      type: new FormControl("defaultPoints", {}),
    });
  }
}
