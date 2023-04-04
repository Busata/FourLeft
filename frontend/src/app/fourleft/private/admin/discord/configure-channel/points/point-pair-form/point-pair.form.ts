import {FormControl, FormGroup} from "@angular/forms";

export class PointPairForm extends FormGroup {
  readonly rank = this.get('rank') as FormControl;
  readonly point = this.get('point') as FormControl;

  constructor(rank: number) {
    super({
      rank: new FormControl(`${rank}`,  {}),
        point: new FormControl("", {})
    });
  }

}
