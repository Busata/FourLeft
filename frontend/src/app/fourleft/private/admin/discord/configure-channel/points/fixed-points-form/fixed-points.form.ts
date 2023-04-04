import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {PointPairForm} from "../point-pair-form/point-pair.form";
import {FixedPointsCalculatorTo} from "../../../../../../../common/generated/server-models";

export class FixedPointsForm extends FormGroup {
  readonly type = this.get('type') as FormControl;

  readonly pointSystem = this.get('pointSystem') as FormGroup;
  readonly defaultStandingPoint = this.get('pointSystem.defaultStandingPoint') as FormControl;
  readonly defaultPowerstagePoint = this.get('pointSystem.defaultPowerstagePoint') as FormControl;
  readonly defaultDNFPoint = this.get('pointSystem.defaultDNFPoint') as FormControl;
  readonly standingPoints = this.get('pointSystem.standingPoints') as FormArray;
  readonly powerStagePoints = this.get('pointSystem.powerStagePoints') as FormArray;

  constructor(value: FixedPointsCalculatorTo) {
    super({
      type: new FormControl("fixedPoints", {}),
      pointSystem: new FormGroup({
        defaultStandingPoint: new FormControl(value?.pointSystemTo?.defaultStandingPoint, {}),
        defaultPowerstagePoint: new FormControl(value?.pointSystemTo?.defaultPowerstagePoint, {}),
        defaultDNFPoint: new FormControl(value?.pointSystemTo?.defaultDNFPoint,{}),
        standingPoints: new FormArray([]),
        powerStagePoints: new FormArray([]),
      })
    });
  }

  public addStandingPoints() {
    this.standingPoints.push(new PointPairForm(this.standingPoints.length + 1));
  }

  public removeStandingsPair(index: number) {
    this.standingPoints.removeAt(index);
  }

  public addPowerPoints() {
    this.powerStagePoints.push(new PointPairForm(this.powerStagePoints.length + 1));
  }

  public removePowerpoints(index: number) {
      this.powerStagePoints.removeAt(index);
  }
}
