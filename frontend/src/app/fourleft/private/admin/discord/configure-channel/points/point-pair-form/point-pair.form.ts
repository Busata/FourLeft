import {FormControl, FormGroup} from "@angular/forms";
import {PointPair} from "../../../../../../../common/generated/server-models";

export class PointPairForm extends FormGroup {
  readonly rank = this.get('rank') as FormControl;
  readonly point = this.get('point') as FormControl;

  constructor(pointPair: PointPair) {
    super({
      rank: new FormControl(pointPair.rank,  {}),
        point: new FormControl(pointPair.point, {})
    });
  }

}
