import {FormControl, FormGroup} from "@angular/forms";
import {StandingPointPairTo} from "@server-models";

export class PointPairForm extends FormGroup {
  readonly rank = this.get('rank') as FormControl;
  readonly point = this.get('point') as FormControl;

  constructor(pointPair: StandingPointPairTo) {
    super({
      rank: new FormControl(pointPair.rank,  {}),
        point: new FormControl(pointPair.point, {})
    });
  }

}
