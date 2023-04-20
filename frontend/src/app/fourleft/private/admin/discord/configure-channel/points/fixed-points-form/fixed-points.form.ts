import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {PointPairForm} from "../point-pair-form/point-pair.form";
import {FixedPointsCalculatorTo} from "@server-models";

export class FixedPointsForm extends FormGroup {
  readonly type = this.get('type') as FormControl;

  readonly pointSystem = this.get('pointSystem') as FormGroup;
  readonly defaultStandingPoint = this.get('pointSystem.defaultStandingPoint') as FormControl;
  readonly defaultPowerstagePoint = this.get('pointSystem.defaultPowerstagePoint') as FormControl;
  readonly defaultDNFPoint = this.get('pointSystem.defaultDNFPoint') as FormControl;
  readonly standingPoints = this.get('pointSystem.standingPoints') as FormArray;
  readonly powerStagePoints = this.get('pointSystem.powerStagePoints') as FormArray;
  readonly joinChampionshipsCount = this.get('joinChampionshipsCount') as FormControl;

  constructor(value: FixedPointsCalculatorTo) {
    super({
      type: new FormControl("fixedPoints", {}),
      joinChampionshipsCount: new FormControl(value?.joinChampionshipsCount, {}),
      pointSystem: new FormGroup({
        defaultStandingPoint: new FormControl(value?.pointSystem?.defaultStandingPoint, {}),
        defaultPowerstagePoint: new FormControl(value?.pointSystem?.defaultPowerstagePoint, {}),
        defaultDNFPoint: new FormControl(value?.pointSystem?.defaultDNFPoint,{}),
        standingPoints: new FormArray(value?.pointSystem?.standingPoints?.map((pair) => new PointPairForm(pair)) || []),
        powerStagePoints: new FormArray(value?.pointSystem?.powerStagePoints?.map((pair) => new PointPairForm(pair)) || [])
      })
    });
  }

  public addStandingPoints() {
    this.standingPoints.push(new PointPairForm({ rank: this.standingPoints.length + 1, point: 0}));
  }

  public removeStandingsPair(index: number) {
    this.standingPoints.removeAt(index);
  }

  public addPowerPoints() {
    this.powerStagePoints.push(new PointPairForm({rank: this.powerStagePoints.length + 1, point: 0}));
  }

  public removePowerpoints(index: number) {
      this.powerStagePoints.removeAt(index);
  }
}
